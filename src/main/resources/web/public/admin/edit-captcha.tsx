import { useEffect, useRef, useState } from 'react';
import Form, { FormInput } from './_common/Form';
import render from './_common/_render';
import RegionSelector from './_common/RegionSelector';
import { Rect, RegionChallenge } from './_common/_captchas';

function EditCaptcha() {
	const challenge = useRef<RegionChallenge>(JSON.parse(document.body.dataset.app).challenge);
	const [preview, setPreview] = useState('');
	const [rect, setRect] = useState<Rect>({
		x: challenge.current.x,
		y: challenge.current.y,
		width: challenge.current.width,
		height: challenge.current.height
	});
	const objectUrl = useRef('');

	useEffect(() => {
		return () => {
			if (objectUrl.current) {
				URL.revokeObjectURL(objectUrl.current);
			}
		};
	}, []);

	function handleFile(e: React.ChangeEvent<HTMLInputElement>) {
		const file = e.target.files?.[0];

		if (objectUrl.current) {
			URL.revokeObjectURL(objectUrl.current);
			objectUrl.current = '';
		}

		if (!file) {
			setPreview('');
			return;
		}

		const url = URL.createObjectURL(file);
		objectUrl.current = url;
		setPreview(url);
	}

	const imageSrc = preview || challenge.current.imageUrl;

	return (
		<>
			<Form enctype="multipart/form-data" action={`/admin/captchas/${challenge.current.id}/edit`}>
				<FormInput label="Replace image">
					<input type="file" name="image" accept="image/jpeg,image/png" onChange={handleFile} />
				</FormInput>
				<FormInput label="Instructions">
					<input type="text" name="instructions" required maxLength={255}
						defaultValue={challenge.current.instructions} />
				</FormInput>
				<div>
					{imageSrc ?
						<RegionSelector src={imageSrc} initialRect={rect} onChange={setRect} /> :
						<div className="error">Image unavailable, upload a new image.</div>
					}
				</div>
				<FormInput>
					<button type="submit" disabled={!imageSrc}>Save captcha</button>
				</FormInput>
			</Form>
			<hr />
			<Form action={`/admin/captchas/${challenge.current.id}/delete`}>
				<FormInput>
					<button type="submit" className="warn-button" style={{
						float: 'unset'
					}}>Delete captcha</button>
				</FormInput>
			</Form>
		</>
	)
}

render(<EditCaptcha />);
