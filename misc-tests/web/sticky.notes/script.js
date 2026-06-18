// Not part of the experiment,
// It just helps dismiss the dialog/sidebar being used for the lab notes.
experimentDialog.addEventListener('click', ({target:dialog}) => {
  if (dialog.nodeName === 'DIALOG') {
    dialog.close('dismiss');
  }
});