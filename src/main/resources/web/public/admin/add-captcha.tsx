import { useEffect, useRef, useState } from 'react';
import Form, { FormInput } from './_common/Form';
import render from './_common/_render';
import RegionSelector from './_common/RegionSelector';
import { Rect } from './_common/_captchas';

function AddCaptcha() {
	const [preview, setPreview] = useState('');
	const [rect, setRect] = useState<Rect | null>(null);
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
		setRect(null);

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

	return (
		<Form enctype="multipart/form-data">
			<FormInput label="Image">
				<input type="file" name="image" accept="image/jpeg,image/png" required onChange={handleFile} />
			</FormInput>
			<FormInput label="Instructions">
				<input type="text" name="instructions" required maxLength={255}
					placeholder="e.g. click the dog's nose" />
			</FormInput>
			<div>
				{preview ?
					<RegionSelector src={preview} onChange={setRect} /> :
					<div style={{ color: '#bbb' }}>Select an image to choose the valid answer area.</div>
				}
			</div>
			<FormInput>
				<button type="submit" disabled={!preview || !rect}>Add captcha</button>
			</FormInput>
		</Form>
	)
}

render(<AddCaptcha />);
