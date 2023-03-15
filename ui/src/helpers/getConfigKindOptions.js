export function getConfigKindOptions() {
  // Provide the available config formats
  const options = [
    "STREAM",
    "DEPLOYMENT",
  ];

  return options.map((option) =>
    Object.assign({}, { value: option, label: option })
  );
}
