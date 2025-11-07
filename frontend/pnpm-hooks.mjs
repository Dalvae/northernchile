export function readPackage(pkg, context) {
  if (process.env.NODE_ENV === 'production') {
    context.log(`[PNPM Hook] Entorno de producción detectado. Modificando dependencias de: ${pkg.name}`)

    const removeDevtoolsDeps = (deps) => {
      if (deps) {
        delete deps['@vue/devtools-api']
        delete deps['@vue/devtools-kit']
        delete deps['perfect-debounce']
      }
    }

    removeDevtoolsDeps(pkg.dependencies)
    removeDevtoolsDeps(pkg.devDependencies)
    removeDevtoolsDeps(pkg.optionalDependencies)

    // Si el paquete es 'pinia', eliminamos su dependencia a las devtools.
    if (pkg.name === 'pinia' && pkg.dependencies) {
      delete pkg.dependencies['@vue/devtools-api']
      context.log(`[PNPM Hook] Eliminada la dependencia '@vue/devtools-api' de pinia.`)
    }
  }

  return pkg
}
