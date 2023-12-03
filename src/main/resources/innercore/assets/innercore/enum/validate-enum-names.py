import json


def validate_name_lower(name):
	if name.isupper():
		return name.lower()
	result = ""
	for c in name:
		if c.isupper():
			if result:
				result += "_"
		result += c.lower()
	return result


def validate_name_upper(name):
	if name.isupper():
		name = name.lower()
	result = ""
	next_upper = True
	for c in name:
		if c == "_":
			next_upper = True
			continue
		if next_upper:
			result += c.upper()
			next_upper = False
		else:
			result += c
	return result


def validate_json(json, keyname=None):
	if isinstance(json, str):
		return validate_name_lower(json)
	if isinstance(json, dict):
		result = {}
		if "__typename__" in json:
			del json["__typename__"]
		for key, value in json.items():
			result[validate_name_lower(key)] = validate_json(value, keyname=key)
		result_sorted = {}
		try:
			for key, value in sorted(result.items(), key=lambda x: x[1]):
				result_sorted[key] = value
		except Exception:
			for key, value in sorted(result.items(), key=lambda x: x[0]):
				result_sorted[key] = value
		# if keyname is not None:
		#	result_sorted["__typename__"] = validate_name_upper(keyname)
		return result_sorted
	return json


def validate_json_file(name):
	with open(name, "r") as f:
		result = json.load(f);
	result = validate_json(result)
	print(json.dumps(result, indent="  "))
	with open(name, "w") as f:
		f.write(json.dumps(result, indent="  "));


validate_json_file("enums-11.json")