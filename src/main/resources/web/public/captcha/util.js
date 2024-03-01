async function sha1(msg) {
	const msgUint8 = new TextEncoder().encode(msg);
	const hashBuffer = await crypto.subtle.digest('SHA-1', msgUint8);
	const hashArray = Array.from(new Uint8Array(hashBuffer));
	return hashArray.map(b => b.toString(2).padStart(8, '0')).join('');
}

function allZeroes(str) {
	let strArr = str.split('');
	for (let s of strArr) {
		if (s !== '0') return false;
	}

	return true;
}

export async function pow(challenge, zeroes, attempt = 0) {
	let attemptString = `${challenge}:${attempt.toString(36)}`;
	let a = await sha1(attemptString);
	if (!allZeroes(a.substring(0, zeroes))) {
		return pow(challenge, zeroes, attempt + 1);
	}

	return attemptString;
}