// utils/authMiddleware.js
const { User } = require('../model');
const { verifyToken } = require('./jwt');
const { fail } = require('./respond');

exports.authenticateToken = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization || '';
    const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7).trim() : null;

    if (!token) return fail(res, 'Unauthorized', 401);

    let payload;
    try {
      payload = verifyToken(token);
    } catch (err) {
      return fail(res, 'Session expired, please log in again', 401);
    }

    const user = await User.findByPk(payload.id);
    if (!user) return fail(res, 'Session expired, please log in again', 401);

    req.user = user;
    return next();
  } catch (error) {
    return next(error);
  }
};
