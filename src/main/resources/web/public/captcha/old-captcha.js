(function() {
    const { sitekey } = document.currentScript.dataset;
    console.log(sitekey);

    async function sha1(msg) {
        const msgUint8 = new TextEncoder().encode(msg);
        const hashBuffer = await crypto.subtle.digest('SHA-1', msgUint8);
        const hashArray = Array.from(new Uint8Array(hashBuffer));
        return hashArray.map(b => b.toString(2).padStart(8, '0')).join('');
    }

    function randBytes(length) {
    	const arr = new Uint8Array(length);
    	crypto.getRandomValues(arr);
    	return Array.from(arr).map((b) => b.toString(16).padStart(2, '0')).join("");
    }

    function allZeroes(str) {
    	let strArr = str.split('');
    	for (let s of strArr) {
    		if (s !== '0') return false;
    	}

    	return true;
    }

    async function run(challenge, zeroes, attempt = 0) {
    	let attemptString = `${challenge}:${attempt.toString(36)}`;
    	let a = await sha1(attemptString);
    	if (!allZeroes(a.substring(0, zeroes))) {
    		return run(challenge, zeroes, attempt + 1);
    	}

    	return attemptString;
    }

    async function getToken(elem, payload) {
        elem.parentElement.parentElement.querySelector('[name="snowcaptcha"]').value = '';

        let resp = await fetch('/get-token', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        let respJson = await resp.json();

        if (respJson.success) {
            console.log('Success');
            // TODO: Update visual status and prevent clicking captcha again until expiry
            // TODO: Handle expiry
            elem.parentElement.parentElement.querySelector('[name="snowcaptcha"]').value = respJson.token;
            return;
        }

        // TODO: Figure out why this token sometimes gets called after a success response

        let solvedChallenges = {};

        for (let ch in respJson.challenges) {
            const c = respJson.challenges[ch];
            console.log(c);
            switch (c.type) {
                case 'PROOF_OF_WORK':
                    solvedChallenges[ch] = await run(c.challenge.challenge, c.challenge.difficulty);
                break;
            }
        }

        return getToken(elem, {
            token: respJson.token,
            challenges: solvedChallenges,
            sitekey: payload.sitekey
        });
    }

    function addClass(e, name) {
        e.classList.add(`snowcaptcha-${name}`);
    }

    function handleCaptchaClick(e) {
        getToken(e, {
            token: "",
            challenges: {},
            sitekey
        });
    }

    function createCaptchaButton(rootElem) {
        const e = document.createElement('div');
        e.tabIndex = '0';
        addClass(e, 'button');

        e.addEventListener('keyup', e => {
            if (['Tab', 'Shift', 'Alt', 'Control', 'Escape'].includes(e.key)) return;
            handleCaptchaClick(rootElem);
        });

        e.addEventListener('click', () => {
            handleCaptchaClick(rootElem);
        });

        return e;
    }

    function createCaptchaText() {
        const e = document.createElement('div');
        addClass(e, 'robot');
        e.innerText = 'I\'m not a robot';

        return e;
    }

    function createCaptchaLogo() {
        const e = document.createElement('img');
        e.src = 'https://assets.winterbit.net/img/64_logo_white_bg.png';
        e.loading = 'lazy';
        e.decoding = 'async';
        addClass(e, 'logo');

        return e;
    }

    function createCaptchaLogoName() {
            const e = document.createElement('div');
            e.innerText = 'SnowCaptcha';
            addClass(e, 'logo-name');

            return e;
        }

    function createCaptchaTerms() {
        const e = document.createElement('a');
        e.innerText = 'Privacy';
        e.href = '#';
        e.setAttribute('ref', 'nofollow');
        addClass(e, 'terms');

        return e;
    }

    function createCaptchaLogoAndTerms() {
        const e = document.createElement('div');
        addClass(e, 'logo-and-terms');

        e.appendChild(createCaptchaLogo());
        e.appendChild(createCaptchaLogoName());
        e.appendChild(createCaptchaTerms());

        return e;
    }

    function createCaptchaElement() {
        const e = document.createElement('div');
        addClass(e, 'root');

        e.appendChild(createCaptchaButton(e));
        e.appendChild(createCaptchaText());
        e.appendChild(createCaptchaLogoAndTerms());

        return e;
    }

    function createCaptchaStyles() {
        const e = document.createElement('style');
        e.innerHTML = `
            .snowcaptcha {
                font-family: sans-serif;
            }

            .snowcaptcha-robot {
                margin-left: 8px;
                font-size: 16px;
                font-weight: bold;
                color: #525252;
            }

            .snowcaptcha-button {
                width: 28px;
                height: 28px;
                border: 2px solid #888;
                border-radius: 8px;
                background: #f8f8f8;
                margin-left: 12px;
            }

            .snowcaptcha-button:hover {
                border-color: #3863ca;
            }

            .snowcaptcha-logo {
                height: 35px;
                width: 35px;
                margin-left: auto;
                margin-right: auto;
                display: block;
            }

            .snowcaptcha-logo-name {
                font-size: 12px;
                font-weight: bold;
                color: #555;
            }

            .snowcaptcha-terms {
                font-size: 10px;
                text-decoration: none;
                display: block;
                color: #555;
                font-weight: bold;
            }

            .snowcaptcha-logo-and-terms {
                margin-left: auto;
                margin-right: 8px;
                text-align: center;
            }

            .snowcaptcha-root {
                width: 300px;
                height: 75px;
                border: 1px solid #bbb;
                border-radius: 4px;
                background: #ddd;
                margin: 8px 4px;
                display: flex;
                align-items: center;
            }
        `;

        return e;
    }

    function createCaptchaResponse() {
        const e = document.createElement('input');
        // TODO: Change to hidden
        e.type = 'text';
        e.name = 'snowcaptcha';

        return e;
    }

    document.querySelectorAll('.snowcaptcha').forEach(e => {
        const container = document.querySelectorAll('.snowcaptcha')[0].parentElement;
        container.insertBefore(createCaptchaResponse(), e);
        container.insertBefore(createCaptchaStyles(), e);
        e.appendChild(createCaptchaElement());
    });
})();