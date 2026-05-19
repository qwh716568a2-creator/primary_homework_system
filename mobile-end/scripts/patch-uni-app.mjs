import fs from 'node:fs'
import path from 'node:path'

const uniAppTargetFile = path.resolve(
  process.cwd(),
  'node_modules',
  '@dcloudio',
  'uni-app',
  'dist',
  'uni-app.es.js'
)

if (fs.existsSync(uniAppTargetFile)) {
  const source = fs.readFileSync(uniAppTargetFile, 'utf8')
  const legacyImportLine =
    "import { shallowRef, ref, getCurrentInstance, isInSSRComponentSetup, injectHook } from 'vue';"
  const partialPatchedImportLine =
    "import { shallowRef, ref, getCurrentInstance, injectHook } from 'vue';\nconst isInSSRComponentSetup = false;"

  if (source.includes(legacyImportLine) || source.includes(partialPatchedImportLine)) {
    const patched = source.replace(
      source.includes(legacyImportLine) ? legacyImportLine : partialPatchedImportLine,
      [
        "import { shallowRef, ref, getCurrentInstance } from 'vue';",
        'const isInSSRComponentSetup = false;',
        'const injectHook = (type, hook, target) => {',
        '  if (!target) {',
        '    return undefined;',
        '  }',
        '  const hooks = target[type] || (target[type] = []);',
        '  hooks.push(hook);',
        '  return hook;',
        '};'
      ].join('\n')
    )

    fs.writeFileSync(uniAppTargetFile, patched, 'utf8')
  }
}

const mainJsPluginFile = path.resolve(
  process.cwd(),
  'node_modules',
  '@dcloudio',
  'uni-cli-shared',
  'dist',
  'vite',
  'plugins',
  'mainJs.js'
)

if (fs.existsSync(mainJsPluginFile)) {
  const mainJsPluginSource = fs.readFileSync(mainJsPluginFile, 'utf8')
  const exactMatcher =
    'return id === mainJsPath || id === mainTsPath || id === mainUTsPath;'

  if (mainJsPluginSource.includes(exactMatcher)) {
    fs.writeFileSync(
      mainJsPluginFile,
      mainJsPluginSource.replace(
        exactMatcher,
        'return id.startsWith(mainJsPath) || id.startsWith(mainTsPath) || id.startsWith(mainUTsPath);'
      ),
      'utf8'
    )
  }
}
