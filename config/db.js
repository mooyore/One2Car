const mysql = require('mysql2');

const db = mysql.createPool({
    host: 'localhost',
    user: 'root',
    password: '',
    database: 'project_mobile',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

module.exports = db.promise(); // ใช้ .promise() เพื่อให้เขียน code แบบ async/await ได้ง่ายขึ้น