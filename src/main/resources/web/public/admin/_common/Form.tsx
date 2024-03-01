import { useEffect, useState, useId, ReactElement, useRef, CSSProperties } from 'react'
// @ts-ignore
import css from './Form.module.css'

interface FormProps {
	method?: string
	action?: string
	noCsrf?: boolean
	setHasErrors?: Function
	children?: ReactElement | ReactElement[]
}

interface FormInputProps {
	label?: string
	hidden?: boolean
	styles?: CSSProperties
	error?: string
	children?: ReactElement
	// Allows for _setHasLocalError to be included
	// in the FormInput props without being visible
	[x: string]: any
}

export function FormInput({
	label,
	hidden = false,
	styles = {},
	error,
	_setHasLocalError = () => {},
	children
}: FormInputProps) {
	let [count, setCount] = useState(children.props.defaultValue?.length || 0);
	let [localError, setLocalError] = useState('');

	let child: ReactElement;
	let id: string;

	const inputOnKeyUp: Function | null = children.props.onKeyUp;

	function onKeyUp(e: Event) {
		if (inputOnKeyUp) inputOnKeyUp(e);
		
		let length = (e.target as HTMLInputElement).value.length;
		setCount(length);

		let localErr = (children.props.required && (e.target as HTMLInputElement).value.length === 0) ?
			`${label || 'Value'} is required`: '';
		if (child.props.maxLength && length > child.props.maxLength) {
			localErr = 'Value is too long';
		}
		setLocalError(localErr);
		_setHasLocalError(!!localErr);
	}

	if (label) {
		id = useId();
		child = React.cloneElement(children, {
			id,
			autoComplete: 'off',
			onKeyUp
		});
	} else {
		child = children;
		child.props.onKeyUp = onKeyUp;
	}

	return (
		<div style={styles} className={[css.input, hidden && css.hidden].filter(e=>e/* Remove empty elements from array*/).join(' ')}>
			{label && <>
				<label htmlFor={id}>{label}</label>
				{child.props.required && <span className={css.required}>*</span>}
			</>}
			{child}
			{(error || localError) && <div className={['error', css.error].filter(e => e).join(' ')}>{localError || error}</div>}
			{child.props.maxLength &&
				<span className={css.counter}><span>{count}</span> / {child.props.maxLength}</span>
			}
		</div>
	)
}

export default function Form({
	method = "POST",
	action,
	noCsrf = false,
	setHasErrors = () => {},
	children
}: FormProps) {
	const [csrfToken, setCsrfToken] = useState('');
	const [errors, setErrors] = useState([] as boolean[]);

	useEffect(() => {
		const csrfMeta: HTMLElement | null = document.querySelector('meta[name="csrf"]');
		if (csrfMeta instanceof HTMLMetaElement) {
			setCsrfToken(csrfMeta.content);
		}
	}, []);

	if (Array.isArray(children)) {
		for (let i = 0; i < (children as ReactElement[]).length; i++) {
			if (children[i].props.children?.type !== 'button') {
				children[i].props._setHasLocalError = (v: boolean) => setErrors(e => {
					e[i] = v;
					setHasErrors(e.some(e => e === true));
					return e;
				});
			}
		}
	}

	return (
		<form method={method} action={action}>
			{!noCsrf &&
				<FormInput hidden={true}>
					<input type="text" name="csrf" value={csrfToken} data-csrf={csrfToken}></input>
				</FormInput>
			}
			{children}
		</form>
	)
}