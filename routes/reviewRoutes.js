const express = require('express');
const router = express.Router();
const db = require('../config/db');

// [ 1. เพิ่มรีวิวใหม่ - Add Review ]
// Logic: เช็คว่า Booking = Completed และ ยังไม่เคยรีวิว ถึงจะยอมให้บันทึก
router.post('/add-review', async (req, res) => {
    const { booking_id, user_id, dealer_id, rating, comment } = req.body;

    try {
        // 1. ตรวจสอบสถานะการจอง: ต้องเป็นคนซื้อคนนี้จริง และสถานะต้อง Completed เท่านั้น
        const [booking] = await db.query(
            "SELECT status FROM booking WHERE booking_id = ? AND user_id = ?", 
            [booking_id, user_id]
        );

        if (booking.length === 0) {
            return res.status(404).json({ message: "ไม่พบประวัติการจองนี้ในระบบ" });
        }
        
        if (booking[0].status !== 'Completed') {
            return res.status(400).json({ message: "คุณยังรีวิวไม่ได้ จนกว่าการซื้อขายจะเสร็จสมบูรณ์ (Completed)" });
        }

        // 2. ตรวจสอบว่าเคยรีวิวไปหรือยัง (1 Booking = 1 Review เท่านั้น)
        const [existing] = await db.query("SELECT review_id FROM review WHERE booking_id = ?", [booking_id]);
        if (existing.length > 0) {
            return res.status(400).json({ message: "คุณได้ทำการรีวิวรายการนี้ไปเรียบร้อยแล้ว" });
        }

        // 3. บันทึกลงตาราง reviews
        const sql = "INSERT INTO review (booking_id, user_id, dealer_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        await db.query(sql, [booking_id, user_id, dealer_id, rating, comment]);

        res.json({ message: "บันทึกรีวิวสำเร็จ ขอบคุณสำหรับคะแนนของคุณ!" });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ 2. ดึงรีวิวทั้งหมดของ Dealer รายนั้น - Get Dealer Reviews ]
// ใช้สำหรับโชว์ในหน้า "รายละเอียดร้านค้า" เพื่อให้คนอื่นเห็นคอมเมนต์
router.get('/dealer-reviews/:dealer_id', async (req, res) => {
    const dealerId = req.params.dealer_id;
    try {
        const sql = `
            SELECT r.*, u.first_name, u.last_name, u.profile_image 
            FROM review r
            JOIN user u ON r.user_id = u.user_id
            WHERE r.dealer_id = ?
            ORDER BY r.created_at DESC
        `;
        const [results] = await db.query(sql, [dealerId]);
        
        // ปรับแต่ง URL รูปโปรไฟล์ของคนรีวิวให้พร้อมใช้งาน
        const reviewsWithUrl = results.map(rev => ({
            ...rev,
            profile_url: rev.profile_image 
                ? `http://localhost:3000/uploads/profile_image/${rev.profile_image}` 
                : `http://localhost:3000/uploads/profile_image/default-profile.png`
        }));

        res.json(reviewsWithUrl);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// [ 3. ดึงคะแนนเฉลี่ยและจำนวนรีวิวรวม - Get Dealer Rating Summary ]
// ใช้โชว์หน้าโปรไฟล์ร้านแบบสรุป (เช่น 4.8 ดาว / 25 รีวิว)
router.get('/dealer-rating-summary/:dealer_id', async (req, res) => {
    const dealerId = req.params.dealer_id;
    try {
        const sql = `
            SELECT 
                COUNT(*) as total_reviews, 
                AVG(rating) as average_rating 
            FROM review 
            WHERE dealer_id = ?
        `;
        const [result] = await db.query(sql, [dealerId]);
        const summary = result[0];

        res.json({
            dealer_id: dealerId,
            total_reviews: summary.total_reviews || 0,
            // ปรับทศนิยม 1 ตำแหน่ง เช่น 4.5
            average_rating: summary.average_rating ? parseFloat(summary.average_rating).toFixed(1) : "0.0"
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = router;