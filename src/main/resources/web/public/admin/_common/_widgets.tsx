import { useRef } from 'react';
import Form, { FormInput } from './Form';

export interface Widget {
	sitekey: string
	secretkey?: string
	name: string
	hashedSecretKey: boolean
}

interface Props {
	type: 'add' | 'edit'
}

export function WidgetForm({ type }: Props) {
	const widget = useRef<Widget>(type === 'edit' ?  JSON.parse(document.body.dataset.app).widget : {
		name: '',
		sitekey: ''
	});

	return (
		<Form>
			<FormInput label="Name">
				<input type="text" name="name" defaultValue={widget.current.name} required maxLength={40} />
			</FormInput>
			<FormInput>
				<button type="submit">{type === 'edit' ? 'Rename' : 'Add'} widget</button>
			</FormInput>
		</Form>
	)
}