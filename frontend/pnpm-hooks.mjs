export function readPackage(pkg, context) {
  // Este hook solo se aplica cuando NODE_ENV es 'production'
  if (process.env.NODE_ENV === 'production') {
    context.log(`[PNPM Hook] Entorno de producción detectado. Modificando dependencias de: ${pkg.name}`);

    // Si el paquete es 'pinia', eliminamos su dependencia a las devtools.
    // Esta es la "cirugía" que impide la fuga.
    if (pkg.name === 'pinia' && pkg.dependencies) {
      delete pkg.dependencies['@vue/devtools-api'];
      context.log(`[PNPM Hook] Eliminada la dependencia '@vue/devtools-api' de pinia.`);
    }
  }

  // Devuelve el package.json (modificado o no) para continuar con la instalación.
  return pkg;
}