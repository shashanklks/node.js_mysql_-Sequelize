// utils/jwt.js
const jwt = require('jsonwebtoken');

const SECRET = (process.env.JWT_SECRET || 'khatabook-dev-secret').trim();
const EXPIRES_IN = process.env.JWT_EXPIRES_IN || '30d';

const generateToken = (user) => jwt.sign({ id: user.id }, SECRET, { expiresIn: EXPIRES_IN });

const verifyToken = (token) => jwt.verify(token, SECRET);

module.exports = { generateToken, verifyToken, SECRET, EXPIRES_IN };
