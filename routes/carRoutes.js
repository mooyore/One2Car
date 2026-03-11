const express = require('express');
const router = express.Router();
const db = require('../config/db');
const multer = require('multer');
const path = require('path');
const fs = require('fs'); // เพิ่มเพื่อใช้ลบไฟล์จริง

// --- ตั้งค่าการเก็บรูปรถ ---
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/car_images/');
    },
    filename: (req, file, cb) => {
        cb(null, 'car-' + Date.now() + '-' + Math.round(Math.random() * 1E9) + path.extname(file.originalname));
    }
});
const upload = multer({ storage: storage });

// [ 0. ดึงข้อมูลยี่ห้อรถทั้งหมด ]
router.get('/brands', async (req, res) => {
    try {
        const [brands] = await db.query("SELECT brand_id, brand_name FROM brand ORDER BY brand_name");
        res.json(brands);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ 1. ฝั่ง USER: ดูข้อมูล และ ค้นหา ]

// ดึงรถทั้งหมด (เฉพาะที่ว่าง)
router.get('/cars', async (req, res) => {
    try {
        const sql = `
            SELECT c.*, b.brand_name, 
            (SELECT images_url FROM car_images WHERE car_id = c.car_id LIMIT 1) as thumbnail
            FROM car c
            LEFT JOIN brand b ON c.brand_id = b.brand_id
            WHERE c.status = 'Available'
            ORDER BY c.car_id DESC
        `;
        const [cars] = await db.query(sql);
        const carsWithUrl = cars.map(car => ({
            ...car,
            thumbnail_url: car.thumbnail ? `http://localhost:3000/uploads/car_images/${car.thumbnail}` : null
        }));
        res.json(carsWithUrl);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ค้นหารถแบบละเอียด
router.get('/search-cars', async (req, res) => {
    const { brand_id, model, year, min_price, max_price, transmission, fuel_type, status } = req.query;
    
    let sql = `
        SELECT c.*, b.brand_name, 
        (SELECT images_url FROM car_images WHERE car_id = c.car_id LIMIT 1) as thumbnail
        FROM car c
        LEFT JOIN brand b ON c.brand_id = b.brand_id
        WHERE 1=1
    `;
    let params = [];

    if (brand_id) { sql += " AND c.brand_id = ?"; params.push(brand_id); }
    if (model) { sql += " AND c.model LIKE ?"; params.push(`%${model}%`); }
    if (year) { sql += " AND c.year = ?"; params.push(year); }
    if (transmission) { sql += " AND c.transmission = ?"; params.push(transmission); }
    if (fuel_type) { sql += " AND c.fuel_type = ?"; params.push(fuel_type); }
    
    // จัดการ Status
    if (status) { 
        sql += " AND c.status = ?"; 
        params.push(status); 
    } else { 
        sql += " AND c.status = 'Available'"; 
    }

    if (min_price && max_price) {
        sql += " AND c.price BETWEEN ? AND ?";
        params.push(min_price, max_price);
    }

    try {
        const [results] = await db.query(sql, params);
        const carsWithUrl = results.map(car => ({
            ...car,
            thumbnail_url: car.thumbnail ? `http://localhost:3000/uploads/car_images/${car.thumbnail}` : null
        }));
        res.json(carsWithUrl);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ 2. ฝั่ง DEALER: เพิ่ม แก้ไข ลบ ]

// เพิ่มรถใหม่
router.post('/add-car', upload.array('car_images', 10), async (req, res) => {
    const { dealer_id, brand_id, model, year, price, color, fuel_type, transmission, mileage, engine_capacity, description } = req.body;
    
    console.log('=== ADD CAR REQUEST ===');
    console.log('Body:', req.body);
    console.log('Files:', req.files ? req.files.length : 0);

    // แปลงค่าว่างเป็น null/0 สำหรับ field ตัวเลข
    const safePrice = price && price.toString().trim() !== '' ? price : 0;
    const safeMileage = mileage && mileage.toString().trim() !== '' ? mileage : 0;
    const safeEngine = engine_capacity && engine_capacity.toString().trim() !== '' ? engine_capacity : null;
    const safeYear = year && year.toString().trim() !== '' ? year : null;

    try {
        const carSql = `INSERT INTO car (dealer_id, brand_id, model, year, price, color, fuel_type, transmission, mileage, engine_capacity, description, status) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Available')`;
        const [carResult] = await db.query(carSql, [dealer_id, brand_id, model, safeYear, safePrice, color, fuel_type, transmission, safeMileage, safeEngine, description]);
        const newCarId = carResult.insertId;

        if (req.files && req.files.length > 0) {
            const imageSql = "INSERT INTO car_images (car_id, images_url) VALUES ?";
            const imageData = req.files.map(file => [newCarId, file.filename]);
            await db.query(imageSql, [imageData]);
        }

        res.json({ message: "เพิ่มรถสำเร็จ", car_id: newCarId });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// แก้ไขข้อมูลรถ (ป้องกันข้อมูลหาย)
router.put('/update-car/:car_id', async (req, res) => {
    const carId = req.params.car_id;
    const fields = ['brand_id', 'model', 'year', 'price', 'color', 'fuel_type', 'transmission', 'mileage', 'engine_capacity', 'description', 'status'];
    
    let updateData = {};
    fields.forEach(field => {
        if (req.body[field] !== undefined) {
            updateData[field] = req.body[field];
        }
    });

    if (Object.keys(updateData).length === 0) {
        return res.status(400).json({ message: "กรุณาระบุข้อมูลที่ต้องการแก้ไข" });
    }

    try {
        const [result] = await db.query("UPDATE car SET ? WHERE car_id = ?", [updateData, carId]);
        if (result.affectedRows === 0) return res.status(404).json({ message: "ไม่พบข้อมูลรถ" });
        res.json({ message: "แก้ไขข้อมูลสำเร็จ", updated: Object.keys(updateData) });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ลบรถ (ลบทั้งใน DB และไฟล์จริง)
router.delete('/delete-car/:car_id', async (req, res) => {
    const carId = req.params.car_id;
    try {
        // 1. ดึงชื่อไฟล์รูปภาพทั้งหมดออกมาก่อนจะลบใน DB
        const [images] = await db.query("SELECT images_url FROM car_images WHERE car_id = ?", [carId]);
        
        // 2. ลบไฟล์รูปภาพจริงๆ ออกจากโฟลเดอร์
        images.forEach(img => {
            const filePath = path.join(__dirname, '../uploads/car_images/', img.images_url);
            if (fs.existsSync(filePath)) {
                fs.unlinkSync(filePath); // ลบไฟล์
            }
        });

        // 3. ลบในฐานข้อมูล (car_images จะถูกลบก่อนถ้าลบรถ หรือลบแยกตามลำดับ)
        await db.query("DELETE FROM car_images WHERE car_id = ?", [carId]);
        const [result] = await db.query("DELETE FROM car WHERE car_id = ?", [carId]);

        if (result.affectedRows === 0) return res.status(404).json({ message: "ไม่พบรถที่ต้องการลบ" });
        res.json({ message: "ลบรถและไฟล์รูปภาพเรียบร้อยแล้ว" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


// ดึงข้อมูลรถตาม car_id (สำหรับหน้า Car Detail)
router.get('/car-detail/:car_id', async (req, res) => {
    const carId = req.params.car_id;
    try {
        const sql = `
            SELECT 
                c.*, 
                b.brand_name, 
                d.dealer_name, 
                d.phone AS dealer_phone, 
                (SELECT images_url FROM car_images WHERE car_id = c.car_id LIMIT 1) as thumbnail
            FROM car c
            LEFT JOIN brand b ON c.brand_id = b.brand_id
            LEFT JOIN dealership d ON c.dealer_id = d.dealer_id
            WHERE c.car_id = ?
        `;
        const [results] = await db.query(sql, [carId]);
        
        if (results.length === 0) {
            return res.status(404).json({ message: "ไม่พบข้อมูลรถ" });
        }

        const car = results[0];
        res.json({
            ...car,
            thumbnail_url: car.thumbnail ? `http://localhost:3000/uploads/car_images/${car.thumbnail}` : null
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;