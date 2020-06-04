def match_dict_path(dict: dict, path: str):
    # TODO maybe switch to https://github.com/jmespath/jmespath.py
    path = path.split(".")

    for i, leg in enumerate(path, 1):
        if i < len(path):
            # Only support * at end of path
            if leg == "*":
                raise Exception("Wildcard must be at end of path")
            dict = dict[leg]
        else:
            if leg == "*":
                return dict
            else:
                return {leg: dict[leg]}
