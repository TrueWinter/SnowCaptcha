import render from './_common/_render';
import('./_common/_accounts').then(({default: AccountForm}) => render(<AccountForm type="edit" />))