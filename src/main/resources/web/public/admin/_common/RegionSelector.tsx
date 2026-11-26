// @ts-ignore
import css from './_captchas.module.css';
import { useEffect, useRef, useState } from 'react';
import { Rect } from './_captchas';

interface Props {
	src: string
	initialRect?: Rect | null
	onChange?: (rect: Rect) => void
}

function clamp(value: number) {
	return Math.max(0, Math.min(1, value));
}

function normalizeRect(start: { x: number, y: number }, end: { x: number, y: number }): Rect {
	const x = Math.min(start.x, end.x);
	const y = Math.min(start.y, end.y);
	return {
		x,
		y,
		width: Math.abs(start.x - end.x),
		height: Math.abs(start.y - end.y)
	};
}

export default function RegionSelector({ src, initialRect, onChange }: Props) {
	const [rect, setRect] = useState<Rect | null>(initialRect ?? null);
	const [previewRect, setPreviewRect] = useState<Rect | null>(null);
	const [dragging, setDragging] = useState(false);
	const start = useRef({ x: 0, y: 0 });
	const imageRef = useRef<HTMLImageElement>();

	useEffect(() => {
		setRect(initialRect ?? null);
		setPreviewRect(null);
	}, [src, initialRect?.x, initialRect?.y, initialRect?.width, initialRect?.height]);

	function getPoint(e: React.PointerEvent<HTMLImageElement>) {
		const bounds = imageRef.current.getBoundingClientRect();
		return {
			x: clamp((e.clientX - bounds.left) / bounds.width),
			y: clamp((e.clientY - bounds.top) / bounds.height)
		};
	}

	function pointerDown(e: React.PointerEvent<HTMLImageElement>) {
		e.preventDefault();
		const point = getPoint(e);
		start.current = point;
		setPreviewRect({ x: point.x, y: point.y, width: 0, height: 0 });
		setDragging(true);
		e.currentTarget.setPointerCapture(e.pointerId);
	}

	function pointerMove(e: React.PointerEvent<HTMLImageElement>) {
		if (!dragging) return;
		e.preventDefault();
		setPreviewRect(normalizeRect(start.current, getPoint(e)));
	}

	function pointerUp(e: React.PointerEvent<HTMLImageElement>) {
		if (!dragging) return;
		e.preventDefault();
		const finalRect = normalizeRect(start.current, getPoint(e));
		setDragging(false);
		setPreviewRect(null);

		if (finalRect.width >= 0.005 && finalRect.height >= 0.005) {
			setRect(finalRect);
			if (onChange) {
				onChange(finalRect);
			}
		}
	}

	function pointerCancel() {
		setDragging(false);
		setPreviewRect(null);
	}

	const displayRect = dragging ? previewRect : rect;

	return (
		<div className={css.selector}>
			<div className={css.hint}>Drag over the image to select the valid answer area.</div>
			<div className={css.imageContainer}>
				<img
					ref={imageRef}
					className={css.image}
					src={src}
					draggable={false}
					onPointerDown={pointerDown}
					onPointerMove={pointerMove}
					onPointerUp={pointerUp}
					onPointerCancel={pointerCancel}
				/>
				{displayRect && displayRect.width > 0 && displayRect.height > 0 &&
					<div className={css.selection} style={{
						left: `${displayRect.x * 100}%`,
						top: `${displayRect.y * 100}%`,
						width: `${displayRect.width * 100}%`,
						height: `${displayRect.height * 100}%`
					}}></div>
				}
			</div>
			<input type="hidden" name="x" value={rect ? rect.x : ''} />
			<input type="hidden" name="y" value={rect ? rect.y : ''} />
			<input type="hidden" name="width" value={rect ? rect.width : ''} />
			<input type="hidden" name="height" value={rect ? rect.height : ''} />
		</div>
	)
}
