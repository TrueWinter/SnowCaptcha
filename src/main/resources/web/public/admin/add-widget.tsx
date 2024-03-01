import render from './_common/_render'
import('./_common/_widgets').then(({WidgetForm}) => {
	render(<WidgetForm type="add" />);
});