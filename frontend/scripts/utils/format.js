import numeral from 'numeral';

export function n(value) {
	return numeral(value).format('0,0');
}

export function f(value) {
	return numeral(value).format('0,0.00');
}