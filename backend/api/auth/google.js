const { OAuth2Client } = require('google-auth-library');
const pool = require('../../lib/db');

module.exports = async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const { email, fullName, idToken } = req.body;

    // Validate required fields
    if (!email || !fullName || !idToken) {
      return res.status(400).json({ error: 'Missing required fields: email, fullName, idToken' });
    }

    // Verify Google ID token
    const client = new OAuth2Client(process.env.GOOGLE_WEB_CLIENT_ID);
    const ticket = await client.verifyIdToken({
      idToken: idToken,
      audience: process.env.GOOGLE_WEB_CLIENT_ID
    });

    const payload = ticket.getPayload();
    const googleId = payload.sub;
    
    // Verify that the email matches
    if (payload.email !== email) {
      return res.status(401).json({ error: 'Email mismatch in token verification' });
    }

    // Check if user exists in database
    let user;
    const existingUser = await pool.query(
      'SELECT * FROM users WHERE google_id = $1 OR email = $2',
      [googleId, email]
    );

    if (existingUser.rows.length > 0) {
      // User exists, update their info
      user = existingUser.rows[0];
      await pool.query(
        'UPDATE users SET full_name = $1, google_id = $2, updated_at = CURRENT_TIMESTAMP WHERE id = $3',
        [fullName, googleId, user.id]
      );
    } else {
      // Create new user
      const newUser = await pool.query(
        'INSERT INTO users (email, full_name, google_id, role) VALUES ($1, $2, $3, $4) RETURNING *',
        [email, fullName, googleId, 'client']
      );
      user = newUser.rows[0];
    }

    // Generate a simple token (in production, use JWT)
    const token = Buffer.from(`${user.id}:${email}:${Date.now()}`).toString('base64');

    res.json({
      status: 'success',
      token: token,
      userId: user.id,
      role: user.role,
      email: user.email
    });

  } catch (error) {
    console.error('Google auth error:', error);
    res.status(500).json({ 
      error: 'Authentication failed',
      message: error.message 
    });
  }
}
