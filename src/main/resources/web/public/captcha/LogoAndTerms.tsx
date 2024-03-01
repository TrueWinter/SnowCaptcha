// @ts-ignore
import css from './captcha.css';

interface Props {
	host: string
}

// @ts-ignore
import logoSvg from './captcha.svg';
// Webpack can create an absolute URL with relative parts (such as https://example.com/build/captcha/../captcha/captcha.svg).
// To prevent any issues that may cause, the URL is normalized before use.
const logo = new URL(logoSvg).href;

export default function LogoAndTerms({ host }: Props) {
	return (
		<div className={css['logo-and-terms']}>
			<img className={css.logo} src={logo} loading="lazy" decoding="async" />
			<div className={css['logo-name']}>SnowCaptcha</div>
			<a className={css.terms} href={`${host}/privacy`} target="_blank" rel="nofollow">Privacy</a>
		</div>
	)
}