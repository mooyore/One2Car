const express = require('express');
const router = express.Router();
const db = require('../config/db');
const multer = require('multer');
const path = require('path');

// --- 1. การตั้งค่าการอัปโหลดโลโก้ร้าน ---
const logoStorage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/logo_image/');
    },
    filename: (req, file, cb) => {
        cb(null, 'dealer-logo-' + Date.now() + path.extname(file.originalname));
    }
});


const uploadLogo = multer({ storage: logoStorage });

// [ A. สมัครเปิดเต็นท์รถ ]
router.post('/upgrade-to-dealer', uploadLogo.single('logo_image'), async (req, res) => {
    const { user_id, dealer_name, address, phone } = req.body;
    
    try {
        // 1. เช็คว่าสมัครซ้ำไหม
        const [existing] = await db.query("SELECT dealer_id FROM dealership WHERE user_id = ?", [user_id]);
        if (existing.length > 0) {
            return res.status(400).json({ message: "คุณเป็น Dealer อยู่แล้ว" });
        }

        const logo_filename = req.file ? req.file.filename : 'profile.png';

        // 2. บันทึกข้อมูล
        const dealerSql = `INSERT INTO dealership (user_id, dealer_name, logo_image, address, phone) VALUES (?, ?, ?, ?, ?)`;
        const [result] = await db.query(dealerSql, [user_id, dealer_name, logo_filename, address, phone]);

        // 3. อัปเดต role
        await db.query("UPDATE user SET role = 'dealer' WHERE user_id = ?", [user_id]);

        res.json({ 
            message: "อัปเกรดเป็น Dealer สำเร็จ", 
            dealer_id: result.insertId,
            logo_url: `http://localhost:3000/uploads/logo_image/${logo_filename}`
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ B. ดึงข้อมูลเต็นท์รถ ]
router.get('/dealer-info/:user_id', async (req, res) => {
    const userId = req.params.user_id;
    try {
        const [dealer] = await db.query("SELECT * FROM dealership WHERE user_id = ?", [userId]);
        if (dealer.length === 0) return res.status(404).json({ message: "ไม่พบข้อมูลดีลเลอร์" });

        const dealerData = dealer[0];
        dealerData.logo_url = `http://localhost:3000/uploads/logo_image/${dealerData.logo_image}`;
        res.json(dealerData);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ C. แก้ไขข้อมูลเต็นท์รถ (ป้องกันข้อมูลหาย) ]
router.put('/update-dealer/:dealer_id', uploadLogo.single('logo_image'), async (req, res) => {
    const dealerId = req.params.dealer_id;
    const { dealer_name, address, phone } = req.body;
    
    // สร้าง Object เฉพาะฟิลด์ที่มีการส่งค่ามาจริงๆ
    let updateData = {};
    if (dealer_name) updateData.dealer_name = dealer_name;
    if (address) updateData.address = address;
    if (phone) updateData.phone = phone;

    if (req.file) {
        updateData.logo_image = req.file.filename;
    }

    if (Object.keys(updateData).length === 0) {
        return res.status(400).json({ message: "ไม่พบข้อมูลที่ต้องการอัปเดต" });
    }

    try {
        const [result] = await db.query("UPDATE dealership SET ? WHERE dealer_id = ?", [updateData, dealerId]);
        if (result.affectedRows === 0) return res.status(404).json({ message: "ไม่พบดีลเลอร์" });
        
        res.json({ message: "อัปเดตข้อมูลสำเร็จ", updated: Object.keys(updateData) });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ D. ดึงรายการรถทั้งหมดของ Dealer ]
router.get('/my-cars/:dealer_id', async (req, res) => {
    const dealerId = req.params.dealer_id;
    try {
        const sql = `
            SELECT c.*, b.brand_name,
            (SELECT images_url FROM car_images WHERE car_id = c.car_id LIMIT 1) as thumbnail
            FROM car c
            LEFT JOIN brand b ON c.brand_id = b.brand_id
            WHERE c.dealer_id = ?
            ORDER BY c.car_id DESC
        `;
        const [results] = await db.query(sql, [dealerId]);
        
        const carsWithUrl = results.map(car => ({
            ...car,
            thumbnail_url: car.thumbnail ? `http://localhost:3000/uploads/car_images/${car.thumbnail}` : null
        }));
        
        res.json(carsWithUrl);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ E. ดึง dealer_id จาก user_id ]
router.get('/dealer-by-user/:user_id', async (req, res) => {
    const userId = req.params.user_id;
    try {
        const [dealer] = await db.query("SELECT dealer_id, dealer_name FROM dealership WHERE user_id = ?", [userId]);
        if (dealer.length === 0) return res.status(404).json({ message: "ไม่พบข้อมูลดีลเลอร์" });
        res.json(dealer[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ F. ดึงรถของ Dealer ตาม dealer_id (alias) ]
router.get('/dealer-cars/:dealer_id', async (req, res) => {
    const dealerId = req.params.dealer_id;
    try {
        const sql = `
            SELECT c.*, b.brand_name,
            (SELECT images_url FROM car_images WHERE car_id = c.car_id LIMIT 1) as thumbnail
            FROM car c
            LEFT JOIN brand b ON c.brand_id = b.brand_id
            WHERE c.dealer_id = ?
            ORDER BY c.car_id DESC
        `;
        const [results] = await db.query(sql, [dealerId]);
        
        const carsWithUrl = results.map(car => ({
            ...car,
            thumbnail_url: car.thumbnail ? `http://localhost:3000/uploads/car_images/${car.thumbnail}` : null
        }));
        
        res.json(carsWithUrl);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;