const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const db = require('../config/db');
const saltRounds = 10;
const multer = require('multer');
const path = require('path');



// ตั้งค่าที่เก็บรูปโปรไฟล์
const profileStorage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/profile_image/');
    },
    filename: (req, file, cb) => {
        cb(null, 'profile-' + Date.now() + path.extname(file.originalname));
    }
});
const uploadProfile = multer({ storage: profileStorage });



// [ Register ]
router.post('/register', async (req, res) => {
    const { email, password, first_name, last_name, role, national_id, phone } = req.body;
    try {
        const hashedPassword = await bcrypt.hash(password, saltRounds);
        const sql = "INSERT INTO user (email, password, first_name, last_name, role, national_id, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
        const [result] = await db.query(sql, [email, hashedPassword, first_name, last_name, role || 'user', national_id, phone]);
        res.json({ message: "ลงทะเบียนสำเร็จ", user_id: result.insertId });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ Login ]
router.post('/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const [users] = await db.query("SELECT * FROM user WHERE email = ?", [email]);
        if (users.length > 0) {
            const user = users[0];
            const isMatch = await bcrypt.compare(password, user.password);
            if (isMatch) {
                const { password, ...userData } = user;
                userData.profile_url = `http://localhost:3000/uploads/profile_image/${user.profile_image}`;
                res.json({ message: "Login success", user: userData });
            } else {
                res.status(401).json({ message: "รหัสผ่านไม่ถูกต้อง" });
            }
        } else {
            res.status(404).json({ message: "ไม่พบผู้ใช้งาน" });
        }
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.put('/update-profile/:user_id', uploadProfile.single('profile_image'), async (req, res) => {
    const userId = req.params.user_id;
    const { first_name, last_name, phone } = req.body;
    
    // 1. สร้าง Object เปล่าสำหรับเก็บข้อมูลที่จะอัปเดต
    let updateData = {};

    // 2. เช็คทีละฟิลด์: ถ้ามีค่าส่งมา ถึงจะเพิ่มเข้าไปใน updateData
    if (first_name) updateData.first_name = first_name;
    if (last_name) updateData.last_name = last_name;
    if (phone) updateData.phone = phone;
    
    // 3. ถ้ามีการอัปโหลดรูปใหม่
    if (req.file) {
        updateData.profile_image = req.file.filename;
    }

    // 4. ตรวจสอบว่ามีข้อมูลถูกส่งมาอัปเดตบ้างไหม (ป้องกันกรณีส่งมาแต่ตัวเปล่า)
    if (Object.keys(updateData).length === 0) {
        return res.status(400).json({ message: "ไม่มีข้อมูลที่จะอัปเดต" });
    }

    try {
        // ใช้ SET ? ของ mysql2 จะช่วยจัดการคอลัมน์ที่เหลือให้โดยอัตโนมัติ
        await db.query("UPDATE user SET ? WHERE user_id = ?", [updateData, userId]);
        res.json({ message: "อัปเดตข้อมูลสำเร็จ", updatedFields: Object.keys(updateData) });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.put('/update-user/:user_id', async (req, res) => {
    const userId = req.params.user_id;
    const { first_name, last_name, email, phone } = req.body;
    try {
        await db.query(
            "UPDATE user SET first_name = ?, last_name = ?, email = ?, phone = ? WHERE user_id = ?",
            [first_name, last_name, email, phone, userId]
        );
        res.json({ message: "อัปเดตข้อมูลสำเร็จ" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


// [ ดึงข้อมูลผู้ใช้ - Get User Info ]
router.get('/user-info/:user_id', async (req, res) => {
    const userId = req.params.user_id;
    try {
        const [users] = await db.query(
            "SELECT user_id, email, first_name, last_name, role, phone, profile_image FROM user WHERE user_id = ?",
            [userId]
        );
        if (users.length === 0) {
            return res.status(404).json({ message: "ไม่พบผู้ใช้งาน" });
        }
        const user = users[0];
        user.profile_url = user.profile_image
            ? `http://localhost:3000/uploads/profile_image/${user.profile_image}`
            : null;
        res.json(user);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;