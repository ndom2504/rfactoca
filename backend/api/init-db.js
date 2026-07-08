const pool = require('../lib/db');
const fs = require('fs');
const path = require('path');

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    // Read the schema file
    const schemaPath = path.join(process.cwd(), 'lib', 'schema.sql');
    const schema = fs.readFileSync(schemaPath, 'utf8');

    // Execute the schema
    await pool.query(schema);

    res.json({ 
      success: true, 
      message: 'Database initialized successfully' 
    });

  } catch (error) {
    console.error('Database initialization error:', error);
    res.status(500).json({ 
      error: 'Failed to initialize database',
      message: error.message 
    });
  }
}
