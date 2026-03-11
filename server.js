const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Static Files (เพื่อให้เปิดดูรูปภาพได้)
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Import Routes
const userRoutes = require('./routes/userRoutes');
const carRoutes = require('./routes/carRoutes');
const dealerRoutes = require('./routes/dealerRoutes');
const bookingRoutes = require('./routes/bookingRoutes');
const reviewRoutes = require('./routes/reviewRoutes');
const adminRoutes = require('./routes/adminRoutes');

// Use Routes (กำหนด Path หลักของ API)
app.use('/api', userRoutes);
app.use('/api', carRoutes);
app.use('/api', dealerRoutes);
app.use('/api',bookingRoutes); 
app.use('/api', reviewRoutes); 
app.use('/api', adminRoutes);

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});