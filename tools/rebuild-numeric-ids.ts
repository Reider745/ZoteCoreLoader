import { readFileSync as readFile, writeFileSync as writeFile, existsSync as exists } from 'fs'

let legacyItemIds = JSON.parse(readFile('legacy_item_ids.json', { encoding: "utf-8" }))
let numericIdsPath = 'numeric_ids.json'
let numericIds = JSON.parse(readFile(numericIdsPath, { encoding: "utf-8" }))

for (const key of Object.keys(legacyItemIds).slice()) {
	let colonIndex = key.indexOf(":")
	let untokenizedKey = key.replace(/\./g, '_')
	legacyItemIds[
		colonIndex != -1 ? untokenizedKey.substring(colonIndex + 1) : untokenizedKey
	] = legacyItemIds[key]
	delete legacyItemIds[key]
}

let legacyItemIdsRaw = Object.assign({}, legacyItemIds)

let legacyItemIdsInline = {}
for (const key in legacyItemIds) {
	legacyItemIdsInline[key.replace(/_/g, '')] = key
}

const rebuildNumericIds = numericIds => {
	let changes = 0
	for (const key of Object.keys(numericIds.blocks)) {
		let targetKey = 'item_' + key
		if (!(targetKey in legacyItemIdsRaw)) {
			targetKey = key.replace('stone_', 'stone_block_')
			if (!(targetKey in legacyItemIdsRaw)) {
				if (key in legacyItemIdsInline) {
					targetKey = legacyItemIdsInline[key]
				}
				if (!(targetKey in legacyItemIdsRaw)) {
					targetKey = key
				}
			}
		}
		let prefix = ' '.repeat(4) + 'block/' + targetKey
		if (prefix.length > 57) {
			prefix = prefix.substring(0, 54) + '...'
		} else {
			prefix = prefix + ' '.repeat(57 - prefix.length)
		}
		if (targetKey in legacyItemIds && legacyItemIds[targetKey] < 256) {
			let replacementId = legacyItemIds[targetKey] < 0 ? 255 - legacyItemIds[targetKey] : legacyItemIds[targetKey]
			delete legacyItemIds[targetKey]
			if (replacementId == numericIds.blocks[key]) {
				continue
			}
			console.debug(prefix + numericIds.blocks[key] + ' -> ' + replacementId)
			numericIds.blocks[key] = replacementId
			changes++
		} else if (!(targetKey in legacyItemIdsRaw)) {
			console.log(prefix + numericIds.blocks[key])
		}
	}
	for (const key of Object.keys(numericIds.items)) {
		let prefix = ' '.repeat(4) + 'item/' + key
		if (prefix.length > 57) {
			prefix = prefix.substring(0, 54) + '...'
		} else {
			prefix = prefix + ' '.repeat(57 - prefix.length)
		}
		if (key in legacyItemIds && legacyItemIds[key] >= 256) {
			let replacementId = legacyItemIds[key]
			delete legacyItemIds[key]
			if (replacementId == numericIds.items[key]) {
				continue
			}
			console.debug(prefix + numericIds.items[key] + ' -> ' + replacementId)
			numericIds.items[key] = replacementId
			changes++
		} else if (!(key in legacyItemIdsRaw)) {
			console.log(prefix + numericIds.items[key])
		}
	}
	return changes
}

console.info('Merging numeric ids with legacy...')
let numericIdsChanges = rebuildNumericIds(numericIds)
if (numericIdsChanges > 0) console.info(`Merged ${numericIdsChanges} rebuilt ids!`)
writeFile(numericIdsPath, JSON.stringify(numericIds, null, ' '.repeat(4)))

let index = 0, numericIdsOverridePath
while (exists(numericIdsOverridePath = `numeric_ids_override_${index}.json`)) {
	console.info(`Merging numeric ids overrides ${index} with legacy...`)
	let numericIdsOverride = JSON.parse(readFile(numericIdsOverridePath, { encoding: "utf-8" }))
	let numericIdsOverrideChanges = rebuildNumericIds(numericIdsOverride)
	if (numericIdsOverrideChanges > 0) console.info(`Merged overriden ${numericIdsOverrideChanges} rebuilt ids!`)
	writeFile(numericIdsOverridePath, JSON.stringify(numericIdsOverride, null, ' '.repeat(4)))
	index++
}

let unexpectedIds = { blocks: {}, items: {} }, unexpected = 0

for (const key in legacyItemIds) {
	if (legacyItemIds[key] >= 256) {
		unexpectedIds.items[key] = legacyItemIds[key]
	} else {
		unexpectedIds.blocks[key] = legacyItemIds[key] < 0 ? 255 - legacyItemIds[key] : legacyItemIds[key]
	}
	unexpected++
}

if (unexpected > 0) {
	console.info(`Flushing missing ${unexpected} numeric legacy ids...`)
	writeFile(numericIdsOverridePath, JSON.stringify(unexpectedIds, null, ' '.repeat(4)))
}
