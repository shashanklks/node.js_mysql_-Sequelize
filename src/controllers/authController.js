// controllers/authController.js
const Joi = require('joi');
const { Op } = require('sequelize');
const { User, Otp } = require('../model');
const { generateToken } = require('../utils/jwt');
const { ok, fail, asyncHandler } = require('../utils/respond');

const OTP_TTL_MINUTES = Number(process.env.OTP_TTL_MINUTES || 5);
const OTP_MAX_ATTEMPTS = 5;
// Real SMS delivery is out of scope; outside production the code is returned
// in the response so the app can be used end to end without a gateway.
const EXPOSE_OTP = process.env.NODE_ENV !== 'production';

const publicUser = (user) => ({
  id: user.id,
  name: user.name || [user.firstName, user.lastName].filter(Boolean).join(' ') || null,
  phone: user.phone,
  email: user.email,
  businessName: user.businessName,
  language: user.language,
  profileCompleted: user.profileCompleted,
});

const phoneSchema = Joi.string()
  .pattern(/^[0-9]{10,15}$/)
  .required()
  .messages({ 'string.pattern.base': 'Enter a valid mobile number' });

/* ------------------------------------------------------------------ */
/* Phone + OTP login (the flow the Android app uses)                    */
/* ------------------------------------------------------------------ */

exports.sendOtp = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({ phone: phoneSchema }).validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const { phone } = value;
  const code = String(Math.floor(100000 + Math.random() * 900000));
  const expiresAt = new Date(Date.now() + OTP_TTL_MINUTES * 60 * 1000);

  // Any previously issued code for this number stops working.
  await Otp.update({ consumed: true }, { where: { phone, consumed: false } });
  await Otp.create({ phone, code, expiresAt });

  console.log(`[otp] ${phone} -> ${code}`);

  return ok(
    res,
    { phone, expiresInSeconds: OTP_TTL_MINUTES * 60, ...(EXPOSE_OTP ? { otp: code } : {}) },
    'OTP sent successfully'
  );
});

exports.verifyOtp = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({
    phone: phoneSchema,
    otp: Joi.string()
      .pattern(/^[0-9]{4,6}$/)
      .required()
      .messages({ 'string.pattern.base': 'Enter the 6 digit OTP' }),
  }).validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const { phone, otp } = value;
  const record = await Otp.findOne({
    where: { phone, consumed: false },
    order: [['createdAt', 'DESC']],
  });

  if (!record) return fail(res, 'Please request an OTP first', 400);
  if (record.expiresAt.getTime() < Date.now()) {
    await record.update({ consumed: true });
    return fail(res, 'OTP has expired, please request a new one', 400);
  }
  if (record.attempts >= OTP_MAX_ATTEMPTS) {
    await record.update({ consumed: true });
    return fail(res, 'Too many wrong attempts, please request a new OTP', 429);
  }
  if (record.code !== otp) {
    await record.increment('attempts');
    return fail(res, 'Incorrect OTP', 401);
  }

  await record.update({ consumed: true });

  let user = await User.findOne({ where: { phone } });
  const isNewUser = !user;
  if (!user) user = await User.create({ phone, language: 'en', profileCompleted: false });

  return ok(res, { token: generateToken(user), isNewUser, user: publicUser(user) }, 'Login successful');
});

/* ------------------------------------------------------------------ */
/* Profile                                                             */
/* ------------------------------------------------------------------ */

exports.getProfile = asyncHandler(async (req, res) => ok(res, { user: publicUser(req.user) }));

exports.updateProfile = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({
    name: Joi.string().trim().min(1).max(100),
    businessName: Joi.string().trim().max(120).allow('', null),
    language: Joi.string().valid('en', 'hi', 'mr', 'gu', 'bn', 'ta', 'te', 'kn', 'ml', 'pa'),
  })
    .min(1)
    .validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const user = req.user;
  await user.update({
    ...value,
    profileCompleted: user.profileCompleted || Boolean(value.name || user.name),
  });

  return ok(res, { user: publicUser(user) }, 'Profile updated');
});

/* ------------------------------------------------------------------ */
/* Legacy email + password auth                                        */
/* ------------------------------------------------------------------ */

// bcrypt ships a compiled binding, so it is loaded only when a password
// endpoint is actually called. The phone + OTP flow the app uses never
// needs it, and a machine without a matching binding can still run the API.
const bcrypt = () => require('bcrypt');

exports.registerUser = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({
    firstName: Joi.string().required(),
    lastName: Joi.string().required(),
    username: Joi.string().alphanum().min(3).max(30).required(),
    email: Joi.string().email().required(),
    password: Joi.string().min(6).required(),
  }).validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const { firstName, lastName, username, email, password } = value;

  const existing = await User.findOne({ where: { [Op.or]: [{ email }, { username }] } });
  if (existing) return fail(res, 'User already exists!', 409);

  const user = await User.create({
    firstName,
    lastName,
    username,
    email,
    name: `${firstName} ${lastName}`,
    password: await bcrypt().hash(password, 10),
    profileCompleted: true,
  });

  return ok(res, { token: generateToken(user), user: publicUser(user) }, 'User registered successfully', 201);
});

exports.loginUser = asyncHandler(async (req, res) => {
  const { error, value } = Joi.object({
    email: Joi.string().email().required(),
    password: Joi.string().required(),
  }).validate(req.body);
  if (error) return fail(res, error.details[0].message, 400);

  const user = await User.findOne({ where: { email: value.email } });
  if (!user || !user.password) return fail(res, 'Invalid credentials', 401);

  const isPasswordValid = await bcrypt().compare(value.password, user.password);
  if (!isPasswordValid) return fail(res, 'Invalid credentials', 401);

  return ok(res, { token: generateToken(user), user: publicUser(user) }, 'Login successful');
});

exports.getUserDetails = asyncHandler(async (req, res) => ok(res, publicUser(req.user)));

exports.publicUser = publicUser;
