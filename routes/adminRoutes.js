const express = require('express');
const router = express.Router();
const db = require('../config/db');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// --- ตั้งค่าการเก็บรูปโลโก้แบรนด์ ---
const brandStorage = multer.diskStorage({
    destination: (req, file, cb) => {
        const dir = path.join(__dirname, '../uploads/brand_logos/');
        if (!fs.existsSync(dir)) {
            fs.mkdirSync(dir, { recursive: true });
        }
        cb(null, dir);
    },
    filename: (req, file, cb) => {
        cb(null, 'brand-' + Date.now() + '-' + Math.round(Math.random() * 1E9) + path.extname(file.originalname));
    }
});
const brandUpload = multer({ storage: brandStorage });

// --- [ 1. DASHBOARD: ดูภาพรวมระบบ ] ---
router.get('/admin/dashboard', async (req, res) => {
    try {
        const statsSql = `
            SELECT 
                (SELECT COUNT(*) FROM user WHERE role = 'user') as total_users,
                (SELECT COUNT(*) FROM user WHERE role = 'dealer') as total_dealers,
                (SELECT COUNT(*) FROM car) as total_cars,
                (SELECT COUNT(*) FROM car WHERE status = 'Sold') as total_sold
        `;
        const [stats] = await db.query(statsSql);
        res.json(stats[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// --- [ 2. USER & DEALER MANAGEMENT: จัดการผู้ใช้และร้านค้า ] ---

// ดึงรายชื่อผู้ใช้ทุกคน (ยกเว้น admin)
router.get('/admin/users', async (req, res) => {
    try {
        const sql = `
            SELECT user_id, first_name, last_name, email, role, phone 
            FROM user 
            WHERE role != 'admin' 
            ORDER BY user_id DESC
        `;
        const [users] = await db.query(sql);
        res.json(users);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// แก้ไข Role ของผู้ใช้ (เช่น เปลี่ยน Dealer กลับเป็น User)
// Logic: ถ้าเปลี่ยนจาก Dealer เป็น User ระบบจะลบข้อมูลร้านทิ้งอัตโนมัติ
router.put('/admin/change-role/:user_id', async (req, res) => {
    const userId = req.params.user_id;
    const { new_role } = req.body; // รับค่า 'user' หรือ 'dealer'

    try {
        // 1. อัปเดต Role ในตาราง User
        await db.query("UPDATE user SET role = ? WHERE user_id = ?", [new_role, userId]);

        // 2. ถ้าเปลี่ยนกลับเป็น 'user' ให้ลบข้อมูลในตาราง dealership ทิ้ง
        // (และรถที่เกี่ยวข้องจะหายไปด้วยเพราะเราตั้ง ON DELETE CASCADE ไว้ใน DB)
        if (new_role === 'user') {
            await db.query("DELETE FROM dealership WHERE user_id = ?", [userId]);
        }

        res.json({ message: `เปลี่ยนสถานะเป็น ${new_role} และปรับปรุงข้อมูลร้านค้าเรียบร้อยแล้ว` });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ลบผู้ใช้ถาวร (ลบแล้วหายทั้งคน ทั้งร้าน ทั้งรถ)
router.delete('/admin/delete-user/:user_id', async (req, res) => {
    const userId = req.params.user_id;
    try {
        await db.query("DELETE FROM user WHERE user_id = ?", [userId]);
        res.json({ message: "ลบผู้ใช้งานและข้อมูลที่เกี่ยวข้องทั้งหมดเรียบร้อยแล้ว" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// --- [ 3. CAR MANAGEMENT: จัดการรถทั้งหมดในระบบ ] ---

// ดูรถทุกคันในระบบ (ไม่ว่าจะเป็นของร้านไหน) — JOIN brand เพื่อดึง brand_name
router.get('/admin/all-cars', async (req, res) => {
    try {
        const sql = `
            SELECT c.*, b.brand_name, d.dealer_name, u.email as contact_email
            FROM car c
            LEFT JOIN brand b ON c.brand_id = b.brand_id
            JOIN dealership d ON c.dealer_id = d.dealer_id
            JOIN user u ON d.user_id = u.user_id
            ORDER BY c.car_id DESC
        `;
        const [cars] = await db.query(sql);
        res.json(cars);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ลบรถที่ทำผิดกฎ
router.delete('/admin/delete-car/:car_id', async (req, res) => {
    const carId = req.params.car_id;
    try {
        // ลบรูปภาพรถก่อน (ป้องกัน Error)
        await db.query("DELETE FROM car_images WHERE car_id = ?", [carId]);
        // ลบข้อมูลรถ
        await db.query("DELETE FROM car WHERE car_id = ?", [carId]);
        
        res.json({ message: "ลบรายการรถเรียบร้อยแล้ว" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// --- [ 4. ADMIN PROFILE: ข้อมูลตัวเอง ] ---
router.get('/admin/profile/:user_id', async (req, res) => {
    try {
        const [admin] = await db.query(
            "SELECT user_id, first_name, last_name, email, role FROM user WHERE user_id = ? AND role = 'admin'", 
            [req.params.user_id]
        );
        if (admin.length === 0) return res.status(404).json({ message: "ไม่พบสิทธิ์ Admin" });
        res.json(admin[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// --- [ 5. BRAND MANAGEMENT: จัดการยี่ห้อรถ ] ---

// เพิ่มยี่ห้อรถใหม่ (พร้อมรูปโลโก้)
router.post('/admin/add-brand', brandUpload.single('brand_logo'), async (req, res) => {
    const { brand_name, country_origin } = req.body;

    if (!brand_name || brand_name.trim() === '') {
        return res.status(400).json({ message: "กรุณาระบุชื่อยี่ห้อ" });
    }

    try {
        // ใช้แค่ brand_name เป็นหลัก เพราะ column อื่นอาจไม่มี
        const sql = `INSERT INTO brand (brand_name) VALUES (?)`;
        const [result] = await db.query(sql, [brand_name.trim()]);

        res.json({ message: "เพิ่มยี่ห้อรถเรียบร้อยแล้ว", brand_id: result.insertId });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ลบยี่ห้อรถ
router.delete('/admin/delete-brand/:brand_id', async (req, res) => {
    const brandId = req.params.brand_id;
    try {
        const [result] = await db.query("DELETE FROM brand WHERE brand_id = ?", [brandId]);
        if (result.affectedRows === 0) {
            return res.status(404).json({ message: "ไม่พบยี่ห้อที่ต้องการลบ" });
        }
        res.json({ message: "ลบยี่ห้อเรียบร้อยแล้ว" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;