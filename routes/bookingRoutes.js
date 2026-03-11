const express = require('express');
const router = express.Router();
const db = require('../config/db');

// [ 1. สร้างการจองใหม่ - User กดจอง ]
router.post('/add-booking', async (req, res) => {
    const { user_id, car_id } = req.body;
    // ถ้าไม่ส่ง booking_date มา ใช้วันที่ปัจจุบัน
    const booking_date = req.body.booking_date || new Date().toISOString().split('T')[0];
    
    console.log('=== ADD BOOKING ===', { user_id, car_id, booking_date });
    
    try {
        const [car] = await db.query("SELECT status FROM car WHERE car_id = ?", [car_id]);
        if (car.length === 0 || car[0].status !== 'Available') {
            return res.status(400).json({ message: "รถไม่ว่างหรือถูกจองไปแล้ว" });
        }

        // เริ่มต้นเป็น 'Pending'
        const [result] = await db.query("INSERT INTO booking (user_id, car_id, booking_date, status) VALUES (?, ?, ?, 'Pending')", [user_id, car_id, booking_date]);
        await db.query("UPDATE car SET status = 'Reserved' WHERE car_id = ?", [car_id]);

        res.json({ message: "จองสำเร็จ สถานะรถคือ Reserved (รอการติดต่อ)", booking_id: result.insertId });
    } catch (err) {
        console.error('Add booking error:', err.message);
        res.status(500).json({ error: err.message });
    }
});

// [ 2. ฝั่ง USER: ยกเลิกการจอง ]
// Logic: เฉพาะ Pending ถึงจะกดได้ -> เปลี่ยนเป็น Cancelled -> คืนสถานะรถเป็น Available
router.put('/user-cancel-booking/:booking_id', async (req, res) => {
    const bookingId = req.params.booking_id;
    try {
        const [booking] = await db.query("SELECT car_id, status FROM booking WHERE booking_id = ?", [bookingId]);
        
        if (booking.length === 0) return res.status(404).json({ message: "ไม่พบข้อมูล" });
        if (booking[0].status !== 'Pending') {
            return res.status(400).json({ message: "ไม่สามารถยกเลิกได้ เนื่องจากสถานะไม่ใช่ Pending" });
        }

        const carId = booking[0].car_id;
        await db.query("UPDATE booking SET status = 'Cancelled' WHERE booking_id = ?", [bookingId]);
        await db.query("UPDATE car SET status = 'Available' WHERE car_id = ?", [carId]);

        res.json({ message: "ยกเลิกการจองเรียบร้อยแล้ว (สถานะ Cancelled)" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ 3. ฝั่ง DEALER: ยืนยันปิดการขาย (Complete) ]
// Logic: Dealer กดเมื่อได้รับเงิน -> Booking เป็น Completed -> รถเป็น Sold
router.put('/dealer-complete-booking/:booking_id', async (req, res) => {
    const bookingId = req.params.booking_id;
    try {
        const [booking] = await db.query("SELECT car_id FROM booking WHERE booking_id = ?", [bookingId]);
        if (booking.length === 0) return res.status(404).json({ message: "ไม่พบข้อมูล" });

        const carId = booking[0].car_id;

        // 1. เปลี่ยน Booking เป็น Completed
        await db.query("UPDATE booking SET status = 'Completed' WHERE booking_id = ?", [bookingId]);
        
        // 2. เปลี่ยนสถานะรถเป็น Sold ทันที
        await db.query("UPDATE car SET status = 'Sold' WHERE car_id = ?", [carId]);

        res.json({ message: "ปิดการขายสำเร็จ! สถานะรถคือ Sold และลูกค้าสามารถรีวิวได้แล้ว" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ 4. ดึงข้อมูลประวัติการจอง (แยกตาม User/Dealer) ]
// ฝั่ง User (เพื่อดูสถานะ และปุ่ม [ยกเลิก] หรือ [รีวิว])
router.get('/my-bookings/:user_id', async (req, res) => {
    const userId = req.params.user_id;
    try {
        const sql = `
            SELECT b.*, c.model, c.price, br.brand_name,
            (SELECT images_url FROM car_images WHERE car_id = c.car_id LIMIT 1) as thumbnail
            FROM booking b
            JOIN car c ON b.car_id = c.car_id
            JOIN brand br ON c.brand_id = br.brand_id
            WHERE b.user_id = ?
            ORDER BY b.booking_id DESC
        `;
        const [results] = await db.query(sql, [userId]);
        res.json(results.map(item => ({
            ...item,
            thumbnail_url: item.thumbnail ? `http://10.0.2.2:3000/uploads/car_images/${item.thumbnail}` : null
        })));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// ฝั่ง Dealer (เพื่อดูใครจอง และกดปุ่ม [Complete])
router.get('/dealer-bookings/:dealer_id', async (req, res) => {
    const dealerId = req.params.dealer_id;
    try {
        const sql = `
            SELECT 
                b.*,
                c.car_id,
                c.model,
                c.year,
                c.price,
                c.mileage,
                c.transmission,
                c.fuel_type,
                c.status AS car_status,
                c.created_at,
                c.color,
                c.engine_capacity,
                c.description,
                br.brand_name,
                u.first_name,
                u.last_name,
                u.phone AS customer_phone,
                (SELECT images_url FROM car_images WHERE car_id = c.car_id LIMIT 1) AS thumbnail
            FROM booking b
            JOIN car c ON b.car_id = c.car_id
            JOIN brand br ON c.brand_id = br.brand_id
            JOIN user u ON b.user_id = u.user_id
            WHERE c.dealer_id = ?
            ORDER BY b.booking_id DESC
        `;
        const [results] = await db.query(sql, [dealerId]);

        res.json(results.map(item => ({
            ...item,
            thumbnail_url: item.thumbnail
                ? `http://10.0.2.2:3000/uploads/car_images/${item.thumbnail}`
                : null
        })));
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;