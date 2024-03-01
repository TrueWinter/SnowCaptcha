import { useRef } from 'react';
import Form, { FormInput } from './_common/Form';
import render from './_common/_render';
import { Widget } from './_common/_widgets';

function Widgets() {
	const widgets = useRef<Widget[]>(JSON.parse(document.body.dataset.app).widgets);

	return (
		<table>
			<thead>
				<tr>
					<th>Name</th>
					<th>Sitekey</th>
					<th>Edit</th>
					<th>Delete</th>
				</tr>
			</thead>
			<tbody>
				{widgets.current.length === 0 ?
					<tr>
						<td colSpan={4}>No widgets</td>
					</tr> : widgets.current.map(e => <tr>
						<td>{e.name}</td>
						<td>{e.sitekey}</td>
						<td><a href={`/admin/widgets/${e.sitekey}/edit`}>Edit</a></td>
						<td>
							<Form action={`/admin/widgets/${e.sitekey}/delete`}>
								<FormInput>
									<button type="submit" style={{
										float: 'unset'
									}}>Delete</button>
								</FormInput>
							</Form>
						</td>
					</tr>)
				}
			</tbody>
		</table>
	)
}

render(<Widgets />)