// @ts-ignore
import css from './captcha.css';

import Button from './Button';
import Robot from './Robot';
import LogoAndTerms from './LogoAndTerms';
import { useEffect, useState } from 'react';

export type Status = 'UNSOLVED' | 'IN_PROGRESS' | 'SOLVED';
export type CallbackData = 'LOADED' | 'RESET' | 'SOLVED' | 'EXPIRED' | 'ERROR';

interface Props {
	setResponse: (token: string) => void
	sitekey: string
	callback: string
	host: string
	mode: 'light' | 'dark',
	assetRoot: string
}

export default function Captcha({ setResponse, sitekey, callback, host, mode, assetRoot }: Props) {
	const [error, setError] = useState('');

	function doCallback(data: CallbackData) {
		if (callback in window && typeof window[callback] === 'function') {
			window[callback](data);
		}
	}

	useEffect(() => {
		doCallback('LOADED');

		function handleSitekeyVerificationResponse(resp: Response) {
			if (resp.status !== 200) {
				setError(`Invalid sitekey (error ${resp.status})`);
			}
		}

		fetch(`${host}/validate-sitekey`, {
			method: 'POST',
			body: JSON.stringify({
				sitekey
			})
		}).then(handleSitekeyVerificationResponse)
		.catch(handleSitekeyVerificationResponse);

		return () => doCallback('RESET');
	}, []);

	return (
		<div className={css.root} data-mode={mode}>
			{sitekey ? <>
				{error && <div className={css.error}>{error}</div>}
				<Button setResponse={setResponse} sitekey={sitekey} callback={doCallback} host={host} setError={setError} />
				<Robot />
			</> : <div style={{
				color: 'red'
			}}>Sitekey not provided</div>}
			<LogoAndTerms host={host} assetRoot={assetRoot} />
		</div>
	)
}