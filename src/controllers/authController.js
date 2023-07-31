// controllers/authController.js
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const Joi = require('joi');
const User = require("../model/User");

const generateToken = (user) => {
  const token = jwt.sign({ id: user.id }, "your-secret-key", {
    expiresIn: "1h",
  });
  return token;
};

exports.registerUser = async (req, res) => {
  try {
    const {error} = Joi.object({
      firstName: Joi.string().required(),
      lastName: Joi.string().required(),
      username: Joi.string().alphanum().min(3).max(30).required(),
      email: Joi.string().email().required(),
      password: Joi.string().min(6).required(),
    }).validate(req.body);
    if (error) {
      return res.status(400).json({
        statusCode: 400,
        success: false,
        message: error.details[0].message,
      });
    }

    const { firstName, lastName, username, email, password } = req.body;

    const hashedPassword = await bcrypt.hash(password, 10);

    const isUserExists = await User.findOne({ where: { email }});
    console.log(isUserExists);
    if (isUserExists) {
      res.status(401).json({
        statusCode: 401,
        success: true,
        message: "User already exists!"
      });
    }

    const user = await User.create({
      firstName,
      lastName,
      username,
      email,
      password: hashedPassword,
    });

    const token = generateToken(user);

    res.status(201).json({
      statusCode: 201,
      success: true,
      message: "User registered successfully",
      token,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      statusCode: 500,
      success: false,
      message: "Something went wrong",
    });
  }
};

exports.loginUser = async (req, res) => {
  try {
    const { email, password } = req.body;

    const user = await User.findOne({ where: { email } });

    if (!user) {
      return res.status(401).json({
        statusCode: 401,
        success: false,
        message: "Invalid credentials",
      });
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);

    if (!isPasswordValid) {
      return res.status(401).json({
        statusCode: 401,
        success: false,
        message: "Invalid credentials",
      });
    }

    const token = generateToken(user);

    res.status(200).json({
      statusCode: 200,
      success: false,
      message: "Login successful",
      token,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      statusCode: 500,
      success: false,
      message: "Something went wrong",
    });
  }
};

exports.getUserDetails = async (req, res) => {
  try {
    const user = req.user;

    res.status(200).json({
      statusCode: 200,
      success: false,
      data: {
        id: user.id,
        firstName: user.firstName,
        lastName: user.lastName,
        username: user.username,
        email: user.email,
      },
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({
      statusCode: 500,
      success: false,
      message: "Something went wrong",
    });
  }
};
