import { useRef, useState } from 'react';
import Form, { FormInput } from './Form';

export interface Account {
	username: string
}

interface Props {
	type: 'add' | 'edit'
}

export default function AccountForm({ type }: Props) {
	const account = useRef<Account>(type === 'edit' ? JSON.parse(document.body.dataset.app).account : {
		username: ''
	});
	const password = useRef<HTMLInputElement>();
	const confirmPassword = useRef<HTMLInputElement>();
	const [passwordError, setPasswordError] = useState(null);
	const [hasError, setHasError] = useState(false);

	function checkPasswords() {
		if (password.current.value !== confirmPassword.current.value) {
			setPasswordError('Passwords must match');
		} else {
			setPasswordError(null);
		}
	}

	return (
		<Form setHasErrors={setHasError}>
			<FormInput label="Username">
				<input type="text" name="username" defaultValue={account.current.username} required disabled={type === 'edit'} />
			</FormInput>
			<FormInput label="Password" error={passwordError}>
				<input type="password" name="password" placeholder={type === 'edit' ? 'Leave blank to keep current password' : ''} ref={password}
					onKeyUp={checkPasswords} required={type === 'add'} />
			</FormInput>
			<FormInput label="Confirm Password" error={passwordError}>
				<input type="password" name="confirm-password" ref={confirmPassword} onKeyUp={checkPasswords} required={type === 'add'} />
			</FormInput>
			<FormInput>
				<button type="submit" disabled={!!passwordError || hasError}>{type === 'edit' ? 'Edit' : 'Add'} account</button>
			</FormInput>
		</Form>
	)
}