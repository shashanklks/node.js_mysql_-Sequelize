// routes/authRoutes.js
const express = require('express');
const authController = require('../controllers/authController');
const { authenticateToken } = require('../utils/authMiddleware');

const router = express.Router();

router.post('/register', authController.registerUser);

router.post('/login', authController.loginUser);

router.get('/user-details', authenticateToken, authController.getUserDetails);

module.exports = router;
