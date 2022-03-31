/**
 * Encode and decode, using Buffer.
 * Works on NodeJS (maybe not on a Web Page...)
 */
const data = 'CodezUp';
console.log('---ORIGINAL-----', data);

// Encode String

const encode = Buffer.from(data).toString('base64');
console.log('\n---ENCODED-----', encode);

// Decode String

const decode = Buffer.from(encode, 'base64').toString('utf-8');
console.log('\n---DECODED-----', decode);
