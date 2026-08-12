// utils/respond.js
// Every endpoint answers with the same envelope so the Android client can
// parse one shape everywhere.
const ok = (res, data = {}, message = 'Success', statusCode = 200) =>
  res.status(statusCode).json({ statusCode, success: true, message, data });

const fail = (res, message = 'Something went wrong', statusCode = 400, extra = {}) =>
  res.status(statusCode).json({ statusCode, success: false, message, ...extra });

// Wraps an async handler so a rejected promise becomes a 500 instead of an
// unhandled rejection that kills the process.
const asyncHandler = (handler) => (req, res, next) =>
  Promise.resolve(handler(req, res, next)).catch(next);

module.exports = { ok, fail, asyncHandler };
