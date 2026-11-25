// @ts-ignore
import css from './captcha.css'
import { useEffect, useRef, useState } from 'react'

interface Props {
	challenge: VisualChallenge
	callback: (answer: string) => void
}

export interface BaseVisualChallenge {
	token: string
	img: string
}

export interface TextVisualChallenge extends BaseVisualChallenge {
	type: 'TEXT'
}

export interface RegionVisualChallenge extends BaseVisualChallenge {
	type: 'REGION'
	instructions: string
}

export type VisualChallenge = TextVisualChallenge | RegionVisualChallenge

function clamp(value: number) {
	return Math.max(0, Math.min(1, value));
}

export default function VisualCaptcha({
	challenge,
	callback
}: Props) {
	const formRef = useRef<HTMLFormElement>();
	const refreshRef = useRef<HTMLButtonElement>();
	const imageRef = useRef<HTMLImageElement>();
	const [keyboardPosition, setKeyboardPosition] = useState({ x: 0.5, y: 0.5 });
	const [keyboardActive, setKeyboardActive] = useState(false);

	function validateAnswer(e: Event) {
		e.preventDefault();

		const answerInput: HTMLInputElement = formRef.current.querySelector('input[name="answer"]');
		callback(answerInput.value);
	}

	function submitPoint(x: number, y: number) {
		callback(`${x.toFixed(6)},${y.toFixed(6)}`);
	}

	function handleImageClick(e: React.MouseEvent<HTMLImageElement>) {
		const bounds = e.currentTarget.getBoundingClientRect();
		const x = clamp((e.clientX - bounds.left) / bounds.width);
		const y = clamp((e.clientY - bounds.top) / bounds.height);
		submitPoint(x, y);
	}

	function handleImageKeyDown(e: React.KeyboardEvent<HTMLImageElement>) {
		if (e.key === 'Enter' || e.key === ' ' || e.key === 'Spacebar') {
			e.preventDefault();
			submitPoint(keyboardPosition.x, keyboardPosition.y);
			return;
		}

		const step = e.shiftKey ? 0.005 : 0.02;
		let dx = 0;
		let dy = 0;

		switch (e.key) {
			case 'ArrowLeft':
				dx = -step;
				break;
			case 'ArrowRight':
				dx = step;
				break;
			case 'ArrowUp':
				dy = -step;
				break;
			case 'ArrowDown':
				dy = step;
				break;
			default:
				return;
		}

		e.preventDefault();
		setKeyboardActive(true);
		setKeyboardPosition(position => ({
			x: clamp(position.x + dx),
			y: clamp(position.y + dy)
		}));
	}

	function refresh() {
		// GETNEW will never be a valid answer for either visual captcha. Incorrect captcha
		// answers result in a new challenge being issued.
		callback('GETNEW');
	}

	function handleRefreshKeyPress(e: KeyboardEvent) {
		if (['Tab', 'Shift', 'Alt', 'Control', 'Escape'].includes(e.key)) return;
		(e.target as HTMLDivElement).blur();
		refresh();
	}

	useEffect(() => {
		if (challenge.type !== 'REGION') return;

		setKeyboardPosition({ x: 0.5, y: 0.5 });
		setKeyboardActive(false);

		const frame = requestAnimationFrame(() => imageRef.current?.focus());
		return () => cancelAnimationFrame(frame);
	}, [challenge.token]);

	useEffect(() => {
		if (formRef.current) {
			formRef.current.addEventListener('submit', validateAnswer);
		}
		refreshRef.current.addEventListener('click', refresh);
		refreshRef.current.addEventListener('keyup', handleRefreshKeyPress);

		return () => {
			if (formRef.current) {
				formRef.current.removeEventListener('submit', validateAnswer);
			}
			refreshRef.current.removeEventListener('click', refresh);
			refreshRef.current.removeEventListener('keyup', handleRefreshKeyPress);
		}
	}, []);

	return (
		<div className={css.visual}>
			<div>
				{challenge.type === 'TEXT' ?
					'Please type the text you see below' :
					challenge.instructions
				}
			</div>
			{challenge.type === 'TEXT' ?
				<div className={css.image}>
					<img src={`data:image/jpeg;base64,${challenge.img}`} />
				</div> :
				<div className={css.regionImage}>
					<img
						ref={imageRef}
						src={challenge.img}
						draggable={false}
						tabIndex={0}
						role="button"
						aria-label={challenge.instructions}
						onClick={handleImageClick}
						onKeyDown={handleImageKeyDown}
						onFocus={() => setKeyboardActive(true)}
						onBlur={() => setKeyboardActive(false)}
					/>
					{keyboardActive &&
						<div className={css.keyboardCursor} style={{
							left: `${keyboardPosition.x * 100}%`,
							top: `${keyboardPosition.y * 100}%`
						}}></div>
					}
				</div>
			}
			{challenge.type === 'REGION' &&
				<div className={css.regionHint}>
					Click the image, or use the arrow keys and press Enter to select.
				</div>
			}
			<form className={css.form} ref={formRef}>
				{/* https://www.svgrepo.com/svg/533704/refresh-cw-alt-3 */}
				<button type="button" ref={refreshRef} className={css.refresh} tabIndex={2} autoFocus={false}>
					{/* <!-- Uploaded to: SVG Repo, www.svgrepo.com, Generator: SVG Repo Mixer Tools --> */}
					<svg width="32px" height="32px" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
						<path d="M4.06189 13C4.02104 12.6724 4 12.3387 4 12C4 7.58172 7.58172 4 12 4C14.5006 4 16.7332 5.14727 18.2002 6.94416M19.9381 11C19.979 11.3276 20 11.6613 20 12C20 16.4183 16.4183 20 12 20C9.61061 20 7.46589 18.9525 6 17.2916M9 17H6V17.2916M18.2002 4V6.94416M18.2002 6.94416V6.99993L15.2002 7M6 20V17.2916" stroke="#000000" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
					</svg>
				</button>
				{challenge.type === 'TEXT' &&
					<input type="text" name="answer" autoComplete="off" autoCorrect="off" autoCapitalize="off" spellCheck="false" autoFocus={true} tabIndex={1} maxLength={6} />
				}
				{challenge.type === 'TEXT' &&
					<button type="submit" tabIndex={1}>Submit</button>
				}
			</form>
		</div>
	)
}
