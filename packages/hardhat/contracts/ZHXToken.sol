// SPDX-License-Identifier: MIT
//pragma solidity >=0.6.10 <0.8.20;
pragma solidity ^0.8.20;

/**
 * @title ZHXToken
 * @dev 完全实现 ERC20 标准接口，包含 mint（发行）和 burn（销毁）功能
 * 代币名称：ZHXToken，符号：ZHX
 */
contract ZHX {
    // 代币核心状态变量
    string private _name;          // 代币名称
    string private _symbol;        // 代币符号
    uint8 private _decimals;       // 小数位数（默认18）
    uint256 private _totalSupply;  // 总供应量

    // 余额映射：地址 => 余额
    mapping(address => uint256) private _balances;

    // 授权映射：所有者地址 => 授权地址 => 授权额度
    mapping(address => mapping(address => uint256)) private _allowances;

    // ERC20 标准事件
    event Transfer(address indexed _from, address indexed _to, uint256 _value);
    event Approval(address indexed _owner, address indexed _spender, uint256 _value);

    /**
     * @dev 构造函数：初始化代币基本信息
     */
    constructor() {
        _name = "ZHXToken";       // 学生名拼音缩写对应的代币名称
        _symbol = "ZHX";          // 代币符号
        _decimals = 18;           // 默认小数位为18
        _totalSupply = 0;         // 初始总供应量为0（可通过mint发行）
    }

    /**
     * @dev 实现 ERC20 接口：返回代币名称
     */
    function name() public view returns (string memory) {  // 添加 memory
        return _name;
    }

    /**
     * @dev 实现 ERC20 接口：返回代币符号
     */
    function symbol() public view returns (string memory) {  // 添加 memory
        return _symbol;
    }

    /**
     * @dev 实现 ERC20 接口：返回小数位数
     */
    function decimals() public view returns (uint8) {
        return _decimals;
    }

    /**
     * @dev 实现 ERC20 接口：返回总供应量
     */
    function totalSupply() public view returns (uint256) {
        return _totalSupply;
    }

    /**
     * @dev 实现 ERC20 接口：查询指定地址的余额
     */
    function balanceOf(address _owner) public view returns (uint256 balance) {
        return _balances[_owner];
    }

    /**
     * @dev 实现 ERC20 接口：转账给指定地址
     * @param _to 接收地址
     * @param _value 转账数量
     * @return success 转账是否成功
     */
    function transfer(address _to, uint256 _value) public returns (bool success) {
        // 检查接收地址不为零地址
        require(_to != address(0), "Transfer to zero address");
        // 检查发送者余额充足
        require(_balances[msg.sender] >= _value, "Insufficient balance");

        // 更新余额
        _balances[msg.sender] -= _value;
        _balances[_to] += _value;

        // 触发Transfer事件
        emit Transfer(msg.sender, _to, _value);
        return true;
    }

    /**
     * @dev 实现 ERC20 接口：从指定地址转账（需先授权）
     * @param _from 转出地址
     * @param _to 接收地址
     * @param _value 转账数量
     * @return success 转账是否成功
     */
    function transferFrom(address _from, address _to, uint256 _value) public returns (bool success) {
        // 检查地址有效性
        require(_from != address(0) && _to != address(0), "Invalid address");
        // 检查转出地址余额充足
        require(_balances[_from] >= _value, "Insufficient balance");
        // 检查授权额度充足
        require(_allowances[_from][msg.sender] >= _value, "Allowance exceeded");

        // 更新授权额度和余额
        _allowances[_from][msg.sender] -= _value;
        _balances[_from] -= _value;
        _balances[_to] += _value;

        // 触发Transfer事件
        emit Transfer(_from, _to, _value);
        return true;
    }

    /**
     * @dev 实现 ERC20 接口：授权第三方使用代币
     * @param _spender 被授权地址
     * @param _value 授权额度
     * @return success 授权是否成功
     */
    function approve(address _spender, uint256 _value) public returns (bool success) {
        // 检查被授权地址不为零地址
        require(_spender != address(0), "Approve to zero address");

        // 更新授权额度
        _allowances[msg.sender][_spender] = _value;

        // 触发Approval事件
        emit Approval(msg.sender, _spender, _value);
        return true;
    }

    /**
     * @dev 实现 ERC20 接口：查询授权额度
     * @param _owner 所有者地址
     * @param _spender 被授权地址
     * @return remaining 剩余授权额度
     */
    function allowance(address _owner, address _spender) public view returns (uint256 remaining) {
        return _allowances[_owner][_spender];
    }

    /**
     * @dev 扩展功能：发行代币（增加总供应量）
     * @param _to 接收地址
     * @param _amount 发行数量
     */
    function mint(address _to, uint256 _amount) public {
        require(_to != address(0), "Mint to zero address");
        require(_amount > 0, "Mint amount must be positive");

        // 增加总供应量和接收地址余额
        _totalSupply += _amount;
        _balances[_to] += _amount;

        // 触发Transfer事件（从0地址转入，代表发行）
        emit Transfer(address(0), _to, _amount);
    }

    /**
     * @dev 扩展功能：销毁代币（减少总供应量）
     * @param _amount 销毁数量
     */
    function burn(uint256 _amount) public {
        require(_balances[msg.sender] >= _amount, "Insufficient balance to burn");
        require(_amount > 0, "Burn amount must be positive");

        // 减少总供应量和发送者余额
        _totalSupply -= _amount;
        _balances[msg.sender] -= _amount;

        // 触发Transfer事件（转入0地址，代表销毁）
        emit Transfer(msg.sender, address(0), _amount);
    }
}