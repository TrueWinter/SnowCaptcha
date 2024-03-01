import { useRef } from 'react';
import render from './_common/_render'
import { Widget } from './_common/_widgets';
import Form, { FormInput } from './_common/Form';

// @ts-ignore
import css from './edit-widget.css';

import('./_common/_widgets').then(({WidgetForm}) => {
	function EditWidget() {
		const widget = useRef<Widget>(JSON.parse(document.body.dataset.app).widget);

		return (
			<>
				<WidgetForm type="edit" />
				<hr />
				<div>Site Key: {widget.current.sitekey}</div>
				<br />
				{widget.current.secretkey && !widget.current.hashedSecretKey ? <>
					<div>Secret Key: {widget.current.secretkey}</div>
					<div>For security reasons, the secret key will only be shown this time.</div>
				</> : <div style={{
					display: 'flex',
					alignItems: 'center',
					gap: '12px'
				}}>
					<Form action={`/admin/widgets/${widget.current.sitekey}/reset`}>
						<FormInput>
							<button type="submit" style={{
								float: 'unset'
							}}>Reset Secret Key</button>
						</FormInput>
					</Form>
				</div>}
				<hr />
				<pre className={css.example}>
					<code>
						{'<form>\n' +
							'\t<!-- Put this where you\'d like the captcha to appear. data-mode can be "light" (default) or "dark" -->\n' +
							'\t<div class="snowcaptcha" data-mode="dark"></div>\n' +
						'</form>\n\n' +

						`<script src="${location.origin}/build/captcha/captcha.js" data-sitekey="${widget.current.sitekey}" async></script>`}
					</code>
				</pre>
			</>
		)
	}

	render(<EditWidget />);
});