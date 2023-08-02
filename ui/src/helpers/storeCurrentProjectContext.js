export function storeCurrentProjectContext(project) {
  // reset current project context
  localStorage.removeItem("projectContext");
  localStorage.setItem("projectContext", project);
}
