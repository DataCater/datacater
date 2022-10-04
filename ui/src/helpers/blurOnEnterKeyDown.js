export function blurOnEnterKeyDown(event) {
  // blur target element if Enter key was pressed
  if (event.which === 13) {
    event.target.blur();
  }
}
