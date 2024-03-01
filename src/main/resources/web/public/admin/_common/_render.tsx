import { createRoot } from 'react-dom/client';

export default function render(jsx: React.ReactElement) {
	const root = createRoot(document.getElementById('app'));
	root.render(jsx);
}