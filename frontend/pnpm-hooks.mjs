export function readPackage(pkg, context) {
  const isProduction = process.env.NODE_ENV === 'production' || process.env.VERCEL_ENV === 'production';

  // Always log for debugging
  if (isProduction) {
    context.log(`[PNPM Hook] Production environment detected. Modifying dependencies for: ${pkg.name}`);
  }

  const removeDevtoolsDeps = (deps) => {
    if (deps) {
      const removed = [];
      if (deps['@vue/devtools-api']) {
        delete deps['@vue/devtools-api'];
        removed.push('@vue/devtools-api');
      }
      if (deps['@vue/devtools-kit']) {
        delete deps['@vue/devtools-kit'];
        removed.push('@vue/devtools-kit');
      }
      if (deps['perfect-debounce']) {
        delete deps['perfect-debounce'];
        removed.push('perfect-debounce');
      }
      if (removed.length > 0 && isProduction) {
        context.log(`[PNPM Hook] Removed from ${pkg.name}: ${removed.join(', ')}`);
      }
    }
  };

  // Remove devtools dependencies from all packages in production
  if (isProduction) {
    removeDevtoolsDeps(pkg.dependencies);
    removeDevtoolsDeps(pkg.devDependencies);
    removeDevtoolsDeps(pkg.optionalDependencies);
    removeDevtoolsDeps(pkg.peerDependencies);
  }

  // Always remove devtools from specific packages that cause issues
  const problematicPackages = ['pinia', '@nuxt/ui', '@vueuse/core'];
  if (problematicPackages.includes(pkg.name)) {
    if (pkg.dependencies?.['@vue/devtools-api']) {
      delete pkg.dependencies['@vue/devtools-api'];
      context.log(`[PNPM Hook] Removed @vue/devtools-api from ${pkg.name}`);
    }
    if (pkg.dependencies?.['@vue/devtools-kit']) {
      delete pkg.dependencies['@vue/devtools-kit'];
      context.log(`[PNPM Hook] Removed @vue/devtools-kit from ${pkg.name}`);
    }
  }

  return pkg;
}
