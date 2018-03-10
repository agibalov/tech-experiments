from ansible.module_utils.basic import *

def main():
    module = AnsibleModule(argument_spec={
        "a": {"required": True, "type": "int"},
        "b": {"required": True, "type": "int"}
    })
    sum = module.params["a"] + module.params["b"]
    module.exit_json(changed=False, meta={
        "a": module.params["a"],
        "b": module.params["b"],
        "sum": sum
    })

if __name__ == "__main__":
    main()
