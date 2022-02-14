let theArray = [
  { text: 'One', duh: true },
  { text: 'Two', duh: true },
  { text: 'Three', duh: false },
  { text: 'Four', duh: false },
  { text: 'Five', duh: true },
  { text: 'Six' },
  { text: 'Seven', duh: false },
  { text: 'Eight', duh: true }
];

let otherArray = theArray.map(item => {
    return `[${item.text}]${item.duh ? (item.duh ? '(Edited)' : '') : ''}`;
});

console.log(otherArray.join(', '));

