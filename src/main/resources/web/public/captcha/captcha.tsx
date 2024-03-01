import Root from './Root';
import { Root as ReactRoot, createRoot } from 'react-dom/client';

function createCaptchaResponse() {
	const e = document.createElement('input');
	e.type = 'hidden';
	e.name = 'snowcaptcha';
	e.autocomplete = 'off';

	return e;
}

function getAssetRoot(src: string) {
	const srcParts = src.split('/');
	srcParts.splice(-1);
	return srcParts.join('/');
}

function injectStyles(src: string) {
	return new Promise(resolve => {
		const srcParts = src.split('/');
		srcParts.splice(-1);

		const e = document.createElement('link');
		e.rel = 'stylesheet';
		e.href = `${getAssetRoot(src)}/captcha.css`;
		e.onload = () => resolve(null);

		document.head.appendChild(e);
	});
}

const scriptSrc = new URL((document.currentScript as HTMLScriptElement).src);
const { sitekey,
	callback,
	host = scriptSrc.origin
} = document.currentScript.dataset;

const roots: Record<string, ReactRoot> = {};

function render(elem: HTMLElement, responseElem: HTMLInputElement = null) {
	let respElem;
	if (responseElem === null) {
		respElem = createCaptchaResponse();
	} else {
		respElem = responseElem;
	}

	function setResponse(token: string) {
		respElem.value = token;
	}

	if (roots[elem.dataset.id]) {
		roots[elem.dataset.id].unmount();
	}

	const root = createRoot(elem);
	roots[elem.dataset.id] = root;
	root.render(<Root setResponse={setResponse} sitekey={sitekey} callback={callback} host={host.replace(/\/$/, '')} mode={elem.dataset.mode === 'dark' ? 'dark' : 'light'} assetRoot={getAssetRoot(scriptSrc.href)} />);

	if (responseElem === null) {
		const container = document.querySelectorAll('.snowcaptcha')[0].parentElement;
		container.insertBefore(respElem, elem);
	} else {
		responseElem.value = '';
	}
}

window.SnowCaptcha = Object.freeze({
	reset: (elem: HTMLDivElement) => {
		function error() {
			console.error('Element must be a SnowCaptcha widget');
		}

		if (!elem) return error();
		if (!elem.dataset.id) return error();
		let respElem: HTMLInputElement = elem.parentElement.querySelector('input[name="snowcaptcha"]');
		if (!respElem) return error();
		render(elem, respElem);
	}
});

injectStyles(scriptSrc.href).then(() => {
	document.querySelectorAll('.snowcaptcha').forEach((e: HTMLDivElement) => {
		e.dataset.id = crypto.randomUUID();
		render(e);
	});
});