import { useRef } from 'react';
import render from './_common/_render';
import Form, { FormInput } from './_common/Form';
import { Account } from './_common/_accounts';

function Accounts() {
	const accounts = useRef<Account[]>(JSON.parse(document.body.dataset.app).accounts);

	return (
		<table>
			<thead>
				<tr>
					<th>Username</th>
					<th>Edit</th>
					<th>Delete</th>
				</tr>
			</thead>
			<tbody>
				{accounts.current.length === 0 ?
					<tr>
						<td colSpan={3}>No accounts</td>
					</tr> : accounts.current.map(e => <tr>
						<td>{e.username}</td>
						<td><a href={`/admin/accounts/${e.username}/edit`}>Edit</a></td>
						<td>
							<Form action={`/admin/accounts/${e.username}/delete`}>
								<FormInput>
									<button type="submit" style={{
										float: 'unset'
									}} disabled={accounts.current.length === 1}
									title={accounts.current.length === 1 ? 'Add a new account before deleting this one' : undefined}>Delete</button>
								</FormInput>
							</Form>
						</td>
					</tr>)
				}
			</tbody>
		</table>
	)
}

render(<Accounts />)