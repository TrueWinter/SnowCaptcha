// @ts-ignore
import css from './_common/_captchas.module.css';
import { useRef } from 'react';
import Form, { FormInput } from './_common/Form';
import render from './_common/_render';
import { RegionChallenge } from './_common/_captchas';

function Captchas() {
	const challenges = useRef<RegionChallenge[]>(JSON.parse(document.body.dataset.app).challenges);

	return (
		<table>
			<thead>
				<tr>
					<th>Image</th>
					<th>Instructions</th>
					<th>Edit</th>
					<th>Delete</th>
				</tr>
			</thead>
			<tbody>
				{challenges.current.length === 0 ?
					<tr>
						<td colSpan={4}>No captchas</td>
					</tr> : challenges.current.map(e => <tr key={e.id}>
						<td>
							{e.imageUrl ?
								<img className={css.thumbnail} src={e.imageUrl} /> :
								<span className="error">Image unavailable</span>
							}
						</td>
						<td>{e.instructions}</td>
						<td><a href={`/admin/captchas/${e.id}/edit`}>Edit</a></td>
						<td>
							<Form action={`/admin/captchas/${e.id}/delete`}>
								<FormInput>
									<button type="submit" className="warn-button" style={{
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

render(<Captchas />)
