# This script is used to automatically update new versions vanilla numeric IDs for legacy mod backcomp.
# Because in 1.16 and above vanilla IDs are not static, each update we should generate new list of IDs and use this script
# to filter out new ones and make them not to conflict with previous ones

import json
import os


def block_item_id(block_id):
	return block_id if block_id < 255 else 255 - block_id


def get_all_files_ordered():
	files = ["numeric_ids.json"]
	index = 0
	while True:
		name = f"numeric_ids_override_{index}.json"
		if os.path.isfile(name):
			files.append(name)
			index += 1
		else:
			return files



def load_file_into_map(id_map, file):
	with open(file, "r") as f:
		file_map = json.load(f)
		for scope, ids in file_map.items():
			if scope not in id_map:
				id_map[scope] = {}
			scope_map = id_map[scope]
			for name, id in ids.items():
				if name not in scope_map:
					scope_map[name] = id


def generate_new_map(id_map, file):
	result_map = {}
	with open(file, "r") as f:
		file_map = json.load(f)
		for scope, ids in file_map.items():
			if scope in id_map:
				result_scope = result_map[scope] = {}
				scope_map = id_map[scope]
				cur_id = max(scope_map.values()) + 1
				for name, id in ids.items():
					if name not in scope_map:
						result_scope[name] = cur_id
						cur_id += 1

		base_items_scope = id_map["items"]
		base_blocks_scope = id_map["blocks"]
		items_scope = result_map["items"]
		blocks_scope = result_map["blocks"]
		for name, block_id in list(blocks_scope.items()):
			if name in items_scope:
				del items_scope[name]
			del blocks_scope[name]

			while block_id in blocks_scope.values() or block_id in base_blocks_scope.values() or block_item_id(block_id) in items_scope.values() or block_item_id(block_id) in base_items_scope.values():
				block_id += 1
			blocks_scope[name] = block_id
			items_scope[name] = block_item_id(block_id)

	with open("output_" + file, "w") as f:
		json.dump(result_map, f, indent=4)

	return "output_" + file




if __name__ == "__main__":
	ordered_files = get_all_files_ordered()
	id_map = {}
	result_id_map = {}
	for f in ordered_files[:-1]:
		load_file_into_map(id_map, f)
		load_file_into_map(result_id_map, f)
	load_file_into_map(result_id_map, generate_new_map(id_map, ordered_files[-1]))

	for scope, scope_map in result_id_map.items():
		print("scope check " + scope + ": all_unique=" + str(len(scope_map) == len(set(scope_map.values()))))
