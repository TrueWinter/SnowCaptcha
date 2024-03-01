import Form, { FormInput } from './_common/Form';
import render from './_common/_render'

function Login() {
	return (
		<Form noCsrf>
			<FormInput label="Username">
				<input type="text" name="username" required />
			</FormInput>
			<FormInput label="Password">
				<input type="password" name="password" required />
			</FormInput>
			<FormInput styles={{
				textAlign: 'center'
			}}>
				<button type="submit" style={{
					float: 'unset'
				}}>Log in</button>
			</FormInput>
		</Form>
	)
}

render(<Login />);