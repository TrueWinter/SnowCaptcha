// @ts-ignore
import css from './captcha.css';

import { CallbackData, Status } from './Root';
import { useEffect, useRef, useState } from 'react';
import { pow } from './util';
import VisualCaptcha, { TextVisualChallenge } from './VisualCaptcha';
import { FloatingArrow, FloatingFocusManager, arrow, autoUpdate, offset, useClick, useDismiss, useFloating, useInteractions, useRole } from '@floating-ui/react';
import { flip, shift } from '@floating-ui/core';

interface Props {
	setResponse: (token: string) => void
	sitekey: string
	callback: (data: CallbackData) => void
	host: string
	setError: (error: string) => void
}

interface Payload {
	token: string
	challenges: Record<string, any>
	sitekey: string
}

interface Response {
	success: boolean
	challenges: Record<string, any>
	token: string
	expiry: number
}

export default function Button({ setResponse, sitekey, callback, host, setError }: Props) {
	const [status, setStatus] = useState<Status>('UNSOLVED');
	const buttonRef = useRef<HTMLDivElement>(null);
	const timer = useRef<NodeJS.Timeout | null>(null);
	const [isVisualCaptchaShown, setIsVisualCaptchaShown] = useState(false);
	const [visualChallenge, setVisualChallenge] = useState<TextVisualChallenge>();
	const tokenRef = useRef(null);
	const arrowRef = useRef(null);
	const solvedChallenges = useRef({});

	function stopTimer() {
		if (timer.current !== null) {
			clearTimeout(timer.current);
			timer.current = null;
		}
	}

	useEffect(() => {
		return stopTimer
	}, []);

	function error(error: string) {
		console.error(error);
		callback('ERROR');
		setStatus('UNSOLVED');
		setError('An error occurred, please try again');
	}
	
	async function getToken(payload: Payload) {
		setStatus('IN_PROGRESS');
		setResponse('');
		setError('');
	
		let respJson: Response;
		try {
			let resp = await fetch(`${host}/get-token`, {
				method: 'POST',
				body: JSON.stringify(payload)
			});

			if (resp.status === 404) {
				tokenRef.current = null;
				return getToken({
					token: '',
					challenges: {},
					sitekey
				});
			}

			respJson = await resp.json();
			solvedChallenges.current = {};
		} catch (e) {
			error(e);
			return;
		}

		stopTimer();
		timer.current = setTimeout(() => {
			setStatus('UNSOLVED');
			setResponse('');
			callback('EXPIRED');
			setError('Token expired, please try again');
			setVisualChallenge(null);
			setIsVisualCaptchaShown(false);
			tokenRef.current = null;
			solvedChallenges.current = {};
		}, (respJson.expiry * 1000) - Date.now());
	
		if (respJson.success) {
			setResponse(respJson.token);
			setStatus('SOLVED');
			callback('SOLVED');
			solvedChallenges.current = {};
			return;
		}
	
		tokenRef.current = respJson.token;
	
		for (let ch in respJson.challenges) {
			const c = respJson.challenges[ch];
			switch (c.type) {
				case 'PROOF_OF_WORK':
					try {
						solvedChallenges.current[ch] = await pow(c.challenge.challenge, c.challenge.difficulty);
					} catch (e) {
						error(e);
						return;
					}
				break;
				case 'TEXT':
					setVisualChallenge({
						token: ch,
						...c.challenge
					});
					setIsVisualCaptchaShown(true);
					// The spinner can be a bit distracting when completing a visual captcha
					setStatus('UNSOLVED');
					break;
			}
		}

		if (Object.keys(solvedChallenges.current).length !== Object.keys(respJson.challenges).length) {
			return;
		}

		setIsVisualCaptchaShown(false);
	
		return getToken({
			token: respJson.token,
			challenges: solvedChallenges.current,
			sitekey: payload.sitekey
		});
	}
	
	function handleCaptchaKeyPress(e: KeyboardEvent) {
		if (['Tab', 'Shift', 'Alt', 'Control', 'Escape'].includes(e.key)) return;
		(e.target as HTMLDivElement).blur();
		handleCaptchaClick();
	}

	function getCurrentState<T>(setFn: React.Dispatch<React.SetStateAction<T>>): Promise<T> {
		return new Promise(resolve => {
			setFn((s: T) => {
				resolve(s);
				return s;
			})
		});
	}

	async function handleCaptchaClick() {
		const currentStatus = await getCurrentState(setStatus);
		const currentVCShownStatus = await getCurrentState(setIsVisualCaptchaShown);

		if (currentStatus !== 'UNSOLVED' || currentVCShownStatus) {
			return
		}
	
		getToken({
			token: tokenRef.current || '',
			challenges: {},
			sitekey
		});
	}

	async function visualCaptchaCallback(answer: string) {
		const challenge = await getCurrentState(setVisualChallenge);
		solvedChallenges.current[challenge.token] = answer;

		setIsVisualCaptchaShown(false);

		getToken({
			token: tokenRef.current,
			challenges: solvedChallenges.current,
			sitekey
		});
	}

	useEffect(() => {
		buttonRef.current.addEventListener('keyup', handleCaptchaKeyPress);
		buttonRef.current.addEventListener('click', handleCaptchaClick);

		return () => {
			buttonRef.current.removeEventListener('keyup', handleCaptchaKeyPress);
			buttonRef.current.removeEventListener('click', handleCaptchaClick);
		}
	}, []);

	const {refs, floatingStyles, context} = useFloating({
		open: isVisualCaptchaShown,
		onOpenChange: (open, event) => {
			if (!open && event.target !== buttonRef.current) {
				setIsVisualCaptchaShown(false);
				setStatus('UNSOLVED');
			}
		},
		middleware: [offset(10), flip(), shift({
			padding: 16
		}), arrow({
			element: arrowRef
		})],
		placement: 'top',
		whileElementsMounted: autoUpdate,
		elements: {
			reference: buttonRef.current
		}
	});

	const click = useClick(context);
	const dismiss = useDismiss(context);
	const role = useRole(context);

	const {getReferenceProps, getFloatingProps} = useInteractions([
		click, dismiss, role
	]);

	return (
		<>
			<div className={[status === 'IN_PROGRESS' ? css['button-spinner'] :
				status === 'SOLVED' ? css['button-checkmark'] :
				css.button].join(' ')} tabIndex={0} ref={buttonRef} {...getReferenceProps()}></div>
			{isVisualCaptchaShown && <FloatingFocusManager context={context} modal={false}>
				<div ref={refs.setFloating} style={floatingStyles} {...getFloatingProps()}>
					<FloatingArrow ref={arrowRef} context={context} className={css.arrow} />
					<VisualCaptcha challenge={visualChallenge} callback={visualCaptchaCallback} />
				</div>
			</FloatingFocusManager>}
		</>
	)
}