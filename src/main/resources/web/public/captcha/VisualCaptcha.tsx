// @ts-ignore
import css from './captcha.css'
import { useEffect, useRef } from 'react'

interface Props {
	challenge: TextVisualChallenge
	callback: (answer: string) => void
}

export interface TextVisualChallenge {
	token: string
	img: string
}

export default function VisualCaptcha({
	challenge,
	callback
}: Props) {
	const formRef = useRef<HTMLFormElement>();
	const refreshRef = useRef<HTMLButtonElement>();

	function validateAnswer(e: Event) {
		e.preventDefault();
		
		const answerInput: HTMLInputElement = formRef.current.querySelector('input[name="answer"]');
		callback(answerInput.value);
	}

	function refresh() {
		// GETNEW will never be a valid answer for the text captcha as it only uses hexadecimal characters.
		// Incorrect captcha answers result in a new challenge being issued.
		callback('GETNEW');
	}

	function handleRefreshKeyPress(e: KeyboardEvent) {
		if (['Tab', 'Shift', 'Alt', 'Control', 'Escape'].includes(e.key)) return;
		(e.target as HTMLDivElement).blur();
		refresh();
	}

	useEffect(() => {
		formRef.current.addEventListener('submit', validateAnswer);
		refreshRef.current.addEventListener('click', refresh);
		refreshRef.current.addEventListener('keyup', handleRefreshKeyPress);

		return () => {
			formRef.current.removeEventListener('submit', validateAnswer);
			refreshRef.current.removeEventListener('click', refresh);
			refreshRef.current.removeEventListener('keyup', handleRefreshKeyPress);
		}
	}, []);

	return (
		<div className={css.visual}>
			<div>Please type the text you see below</div>
			<div className={css.image}>
				<img src={`data:image/jpeg;base64,${challenge.img}`} />
			</div>
			<form className={css.form} ref={formRef}>
				{/* https://www.svgrepo.com/svg/533704/refresh-cw-alt-3 */}
				<button type="button" ref={refreshRef} className={css.refresh} tabIndex={2} autoFocus={false}>
					{/* <!-- Uploaded to: SVG Repo, www.svgrepo.com, Generator: SVG Repo Mixer Tools --> */}
					<svg width="32px" height="32px" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
						<path d="M4.06189 13C4.02104 12.6724 4 12.3387 4 12C4 7.58172 7.58172 4 12 4C14.5006 4 16.7332 5.14727 18.2002 6.94416M19.9381 11C19.979 11.3276 20 11.6613 20 12C20 16.4183 16.4183 20 12 20C9.61061 20 7.46589 18.9525 6 17.2916M9 17H6V17.2916M18.2002 4V6.94416M18.2002 6.94416V6.99993L15.2002 7M6 20V17.2916" stroke="#000000" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
					</svg>
				</button>
				<input type="text" name="answer" autoComplete="off" autoCorrect="off" autoCapitalize="off" spellCheck="false" autoFocus={true} tabIndex={1} maxLength={6} />
				<button type="submit" tabIndex={1}>Submit</button>
			</form>
		</div>
	)
}