// @ts-ignore
import css from './captcha.css';

interface Props {
	host: string
	assetRoot: string
}

// @ts-ignore
import './captcha.svg';

export default function LogoAndTerms({ host, assetRoot }: Props) {
	return (
		<div className={css['logo-and-terms']}>
			<img className={css.logo} src={`${assetRoot}/captcha.svg`} loading="lazy" decoding="async" />
			<div className={css['logo-name']}>SnowCaptcha</div>
			<a className={css.terms} href={`${host}/privacy`} target="_blank" rel="nofollow">Privacy</a>
		</div>
	)
}